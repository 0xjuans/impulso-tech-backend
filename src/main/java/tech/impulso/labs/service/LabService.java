package tech.impulso.labs.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.entity.Course;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.labs.dto.ExecuteCodeRequest;
import tech.impulso.labs.dto.ExecutionResultResponse;
import tech.impulso.labs.dto.LabRequest;
import tech.impulso.labs.dto.LabResponse;
import tech.impulso.labs.dto.LabSubmissionResponse;
import tech.impulso.labs.entity.Lab;
import tech.impulso.labs.entity.LabSubmission;
import tech.impulso.labs.repository.LabRepository;
import tech.impulso.labs.repository.LabSubmissionRepository;
import tech.impulso.lessons.entity.Lesson;
import tech.impulso.lessons.repository.LessonRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.util.List;

/**
 * Servicio con la lógica de negocio de laboratorios y sus entregas
 * (RF-013, RF-037, RF-038).
 *
 * <p>Aplica las reglas de propiedad: sólo el instructor propietario (o
 * el administrador) puede crear, modificar o eliminar un laboratorio;
 * los estudiantes únicamente pueden interactuar con laboratorios en
 * estado {@link ContentStatus#PUBLICADO}. Toda ejecución se delega a
 * {@link CodeExecutionService} para respetar el aislamiento del
 * sandbox (§30 del CLAUDE.md).</p>
 */
@Service
public class LabService {

    /** Longitud máxima aceptada del código enviado. */
    private static final int MAX_CODE_LENGTH = 20_000;

    private final LabRepository labRepository;
    private final LabSubmissionRepository submissionRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final CurrentUserService currentUserService;
    private final CodeExecutionService codeExecutionService;

    public LabService(LabRepository labRepository,
                      LabSubmissionRepository submissionRepository,
                      CourseRepository courseRepository,
                      LessonRepository lessonRepository,
                      CurrentUserService currentUserService,
                      CodeExecutionService codeExecutionService) {
        this.labRepository = labRepository;
        this.submissionRepository = submissionRepository;
        this.courseRepository = courseRepository;
        this.lessonRepository = lessonRepository;
        this.currentUserService = currentUserService;
        this.codeExecutionService = codeExecutionService;
    }

    /**
     * Búsqueda paginada de laboratorios respetando la visibilidad del
     * solicitante.
     */
    @Transactional(readOnly = true)
    public Page<LabResponse> search(String query,
                                    String language,
                                    Long courseId,
                                    Pageable pageable) {
        User caller = currentUserService.requireAuthenticatedUser();
        Long instructorFilter = null;
        ContentStatus statusFilter = null;

        if (caller.getRole() == Role.ESTUDIANTE) {
            statusFilter = ContentStatus.PUBLICADO;
        } else if (caller.getRole() == Role.INSTRUCTOR) {
            instructorFilter = caller.getId();
        }

        boolean showOwnerView = caller.getRole() != Role.ESTUDIANTE;
        return labRepository.search(normalize(query), normalize(language),
                        statusFilter, instructorFilter, courseId, pageable)
                .map(lab -> showOwnerView ? LabResponse.forOwner(lab) : LabResponse.forStudent(lab));
    }

    /**
     * Devuelve el detalle de un laboratorio.
     */
    @Transactional(readOnly = true)
    public LabResponse getById(Long id) {
        User caller = currentUserService.requireAuthenticatedUser();
        Lab lab = requireLab(id);
        if (caller.getRole() == Role.ESTUDIANTE && lab.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El laboratorio indicado no existe.");
        }
        boolean isOwnerOrAdmin = caller.getRole() == Role.ADMINISTRADOR
                || lab.getInstructor().getId().equals(caller.getId());
        return isOwnerOrAdmin ? LabResponse.forOwner(lab) : LabResponse.forStudent(lab);
    }

    /**
     * Crea un nuevo laboratorio con el instructor autenticado como
     * propietario.
     */
    @Transactional
    public LabResponse create(LabRequest request) {
        User instructor = requireInstructor();
        Lab lab = new Lab();
        applyRequest(lab, request);
        lab.setInstructor(instructor);
        return LabResponse.forOwner(labRepository.save(lab));
    }

    /**
     * Actualiza un laboratorio existente. Requiere ser el propietario o
     * administrador.
     */
    @Transactional
    public LabResponse update(Long id, LabRequest request) {
        User caller = currentUserService.requireAuthenticatedUser();
        Lab lab = requireLab(id);
        requireOwnerOrAdmin(caller, lab);
        applyRequest(lab, request);
        return LabResponse.forOwner(labRepository.save(lab));
    }

    /**
     * Elimina un laboratorio. Requiere ser el propietario o administrador.
     */
    @Transactional
    public void delete(Long id) {
        User caller = currentUserService.requireAuthenticatedUser();
        Lab lab = requireLab(id);
        requireOwnerOrAdmin(caller, lab);
        labRepository.delete(lab);
    }

    /**
     * Ejecuta el código enviado dentro del contexto del laboratorio sin
     * persistir la entrega. Sirve al estudiante para probar sus cambios
     * antes de hacer una entrega formal (RF-037).
     */
    @Transactional(readOnly = true)
    public ExecutionResultResponse tryExecute(Long labId, ExecuteCodeRequest request) {
        User caller = currentUserService.requireAuthenticatedUser();
        Lab lab = requireLab(labId);
        assertVisibleForExecution(caller, lab);
        CodeExecutionService.ExecutionResult result = executeSafely(lab, request);
        boolean passed = evaluatePass(lab, result);
        return ExecutionResultResponse.from(result, passed);
    }

    /**
     * Registra una entrega del estudiante y ejecuta el código para
     * capturar el resultado obtenido (RF-038).
     */
    @Transactional
    public LabSubmissionResponse submit(Long labId, ExecuteCodeRequest request) {
        User student = currentUserService.requireAuthenticatedUser();
        if (student.getRole() != Role.ESTUDIANTE) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo los estudiantes pueden enviar entregas de laboratorio.");
        }
        Lab lab = requireLab(labId);
        assertVisibleForExecution(student, lab);

        CodeExecutionService.ExecutionResult result = executeSafely(lab, request);
        boolean passed = evaluatePass(lab, result);

        Integer previous = submissionRepository.findMaxSubmissionNumber(labId, student.getId());
        int number = previous == null ? 1 : previous + 1;

        LabSubmission submission = new LabSubmission();
        submission.setLab(lab);
        submission.setUser(student);
        submission.setSubmissionNumber(number);
        submission.setCode(request.code());
        submission.setStdout(result.stdout());
        submission.setStderr(result.stderr());
        submission.setExitCode(result.exitCode());
        submission.setExecutionTimeMs(result.executionTimeMs());
        submission.setPassed(passed);
        return LabSubmissionResponse.from(submissionRepository.save(submission));
    }

    /**
     * Devuelve las entregas del usuario autenticado sobre un
     * laboratorio.
     */
    @Transactional(readOnly = true)
    public List<LabSubmissionResponse> listMySubmissions(Long labId) {
        User caller = currentUserService.requireAuthenticatedUser();
        Lab lab = requireLab(labId);
        assertVisibleForExecution(caller, lab);
        return submissionRepository
                .findByLabIdAndUserIdOrderBySubmissionNumberDesc(labId, caller.getId()).stream()
                .map(LabSubmissionResponse::from)
                .toList();
    }

    /**
     * Aplica los datos del request a la entidad, resolviendo curso y
     * lección cuando corresponde.
     */
    private void applyRequest(Lab lab, LabRequest request) {
        lab.setTitle(request.title().strip());
        lab.setDescription(request.description());
        lab.setInstructions(request.instructions());
        lab.setLanguage(request.language().trim().toLowerCase());
        lab.setStarterCode(request.starterCode());
        lab.setExpectedOutput(request.expectedOutput());
        lab.setExecutionTimeoutMs(request.executionTimeoutMs());
        lab.setCourse(resolveCourse(request.courseId()));
        lab.setLesson(resolveLesson(request.lessonId()));
        lab.setStatus(request.status() == null ? ContentStatus.BORRADOR : request.status());
    }

    private Course resolveCourse(Long courseId) {
        if (courseId == null) {
            return null;
        }
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El curso indicado no existe."));
    }

    private Lesson resolveLesson(Long lessonId) {
        if (lessonId == null) {
            return null;
        }
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "La lección indicada no existe."));
    }

    private Lab requireLab(Long id) {
        return labRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El laboratorio indicado no existe."));
    }

    private User requireInstructor() {
        User caller = currentUserService.requireAuthenticatedUser();
        if (caller.getRole() != Role.INSTRUCTOR && caller.getRole() != Role.ADMINISTRADOR) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo instructores o administradores pueden crear laboratorios.");
        }
        return caller;
    }

    private void requireOwnerOrAdmin(User caller, Lab lab) {
        boolean isAdmin = caller.getRole() == Role.ADMINISTRADOR;
        boolean isOwner = lab.getInstructor().getId().equals(caller.getId());
        if (!isAdmin && !isOwner) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "No tiene permiso para modificar este laboratorio.");
        }
    }

    private void assertVisibleForExecution(User caller, Lab lab) {
        boolean isOwnerOrAdmin = caller.getRole() == Role.ADMINISTRADOR
                || lab.getInstructor().getId().equals(caller.getId());
        if (!isOwnerOrAdmin && lab.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El laboratorio indicado no existe.");
        }
    }

    private CodeExecutionService.ExecutionResult executeSafely(Lab lab, ExecuteCodeRequest request) {
        if (request.code() != null && request.code().length() > MAX_CODE_LENGTH) {
            throw new BusinessException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "El código enviado supera el tamaño máximo permitido.");
        }
        return codeExecutionService.execute(new CodeExecutionService.ExecutionRequest(
                lab.getLanguage(),
                request.code(),
                request.stdin(),
                lab.getExecutionTimeoutMs()
        ));
    }

    /**
     * Determina si la ejecución se considera aprobada comparando la
     * salida del programa con la salida esperada del laboratorio, tras
     * normalizar espacios finales de cada línea.
     */
    private static boolean evaluatePass(Lab lab, CodeExecutionService.ExecutionResult result) {
        if (!result.success()) {
            return false;
        }
        String expected = lab.getExpectedOutput();
        if (expected == null || expected.isBlank()) {
            return true;
        }
        return normalizeOutput(result.stdout()).equals(normalizeOutput(expected));
    }

    /**
     * Normaliza saltos de línea y espacios finales para comparar salidas
     * de forma tolerante a diferencias triviales de formato.
     */
    private static String normalizeOutput(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\r\n", "\n").stripTrailing();
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
