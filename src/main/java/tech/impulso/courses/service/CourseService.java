package tech.impulso.courses.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.dto.CourseResponse;
import tech.impulso.courses.dto.CreateCourseRequest;
import tech.impulso.courses.dto.UpdateCourseRequest;
import tech.impulso.courses.entity.Course;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.learningroutes.entity.LearningRoute;
import tech.impulso.learningroutes.repository.LearningRouteRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

/**
 * Servicio con las operaciones sobre cursos (RF-040).
 *
 * <p>Contempla las funcionalidades de consulta disponibles para los
 * estudiantes (sólo cursos publicados) y las de gestión reservadas al
 * instructor responsable y a los administradores.</p>
 */
@Service
public class CourseService {

    /** Valor sentinela que desasocia un curso de cualquier ruta. */
    private static final long UNLINK_ROUTE_SENTINEL = -1L;

    private final CourseRepository courseRepository;
    private final LearningRouteRepository learningRouteRepository;
    private final CurrentUserService currentUserService;

    public CourseService(CourseRepository courseRepository,
                         LearningRouteRepository learningRouteRepository,
                         CurrentUserService currentUserService) {
        this.courseRepository = courseRepository;
        this.learningRouteRepository = learningRouteRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve los cursos publicados visibles para cualquier usuario.
     */
    @Transactional(readOnly = true)
    public PagedResponse<CourseResponse> listPublished(String search,
                                                       DifficultyLevel difficulty,
                                                       Long learningRouteId,
                                                       Pageable pageable) {
        Page<Course> page = courseRepository.search(normalize(search), difficulty,
                ContentStatus.PUBLICADO, learningRouteId, pageable);
        return PagedResponse.from(page, CourseResponse::from);
    }

    /**
     * Devuelve todos los cursos incluyendo borradores y deshabilitados,
     * para vistas administrativas o de gestión.
     */
    @Transactional(readOnly = true)
    public PagedResponse<CourseResponse> listAll(String search,
                                                 DifficultyLevel difficulty,
                                                 ContentStatus status,
                                                 Long learningRouteId,
                                                 Pageable pageable) {
        Page<Course> page = courseRepository.search(normalize(search), difficulty,
                status, learningRouteId, pageable);
        return PagedResponse.from(page, CourseResponse::from);
    }

    /**
     * Consulta el detalle de un curso aplicando las reglas de
     * visibilidad correspondientes.
     */
    @Transactional(readOnly = true)
    public CourseResponse findById(Long id) {
        Course course = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanRead(course, current);
        return CourseResponse.from(course);
    }

    /**
     * Crea un nuevo curso. El usuario autenticado queda como instructor
     * responsable.
     */
    @Transactional
    public CourseResponse create(CreateCourseRequest request) {
        User author = currentUserService.requireAuthenticatedUser();
        ensureCanManage(author);

        Course course = new Course();
        course.setName(request.name().trim());
        course.setDescription(request.description());
        course.setObjective(request.objective());
        course.setCoverImageUrl(nullIfBlank(request.coverImageUrl()));
        course.setDifficulty(request.difficulty());
        course.setEstimatedDurationHours(request.estimatedDurationHours());
        course.setTechnology(nullIfBlank(request.technology()));
        course.setGeneratesCertificate(request.generatesCertificate());
        course.setStatus(ContentStatus.BORRADOR);
        course.setInstructor(author);
        if (request.learningRouteId() != null) {
            course.setLearningRoute(loadRoute(request.learningRouteId()));
        }

        return CourseResponse.from(courseRepository.save(course));
    }

    /**
     * Actualiza los campos permitidos de un curso existente.
     */
    @Transactional
    public CourseResponse update(Long id, UpdateCourseRequest request) {
        Course course = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(course, current);

        if (request.name() != null) {
            course.setName(request.name().trim());
        }
        if (request.description() != null) {
            course.setDescription(request.description());
        }
        if (request.objective() != null) {
            course.setObjective(request.objective());
        }
        if (request.coverImageUrl() != null) {
            course.setCoverImageUrl(nullIfBlank(request.coverImageUrl()));
        }
        if (request.difficulty() != null) {
            course.setDifficulty(request.difficulty());
        }
        if (request.estimatedDurationHours() != null) {
            course.setEstimatedDurationHours(request.estimatedDurationHours());
        }
        if (request.technology() != null) {
            course.setTechnology(nullIfBlank(request.technology()));
        }
        if (request.generatesCertificate() != null) {
            course.setGeneratesCertificate(request.generatesCertificate());
        }
        if (request.learningRouteId() != null) {
            if (request.learningRouteId() == UNLINK_ROUTE_SENTINEL) {
                course.setLearningRoute(null);
            } else {
                course.setLearningRoute(loadRoute(request.learningRouteId()));
            }
        }

        return CourseResponse.from(course);
    }

    /**
     * Publica, deshabilita o vuelve a borrador el curso indicado.
     */
    @Transactional
    public CourseResponse changeStatus(Long id, ContentStatus status) {
        Course course = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(course, current);
        course.setStatus(status);
        return CourseResponse.from(course);
    }

    private Course load(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El curso indicado no existe."));
    }

    private LearningRoute loadRoute(Long id) {
        return learningRouteRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "La ruta de aprendizaje indicada no existe."));
    }

    private void ensureCanManage(User user) {
        if (user.getRole() == Role.ESTUDIANTE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No cuenta con permisos para gestionar cursos.");
        }
    }

    private void ensureCanEdit(Course course, User user) {
        ensureCanManage(user);
        boolean isOwner = course.getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Solo el instructor responsable o un administrador puede modificar este curso.");
        }
    }

    private void ensureCanRead(Course course, User user) {
        if (course.getStatus() == ContentStatus.PUBLICADO) {
            return;
        }
        boolean isOwner = course.getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El curso indicado no existe.");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String nullIfBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
