package tech.impulso.enrollments.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.entity.Course;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.enrollments.dto.CourseProgressResponse;
import tech.impulso.enrollments.dto.EnrollmentResponse;
import tech.impulso.enrollments.entity.Enrollment;
import tech.impulso.enrollments.entity.EnrollmentStatus;
import tech.impulso.enrollments.entity.LessonCompletion;
import tech.impulso.enrollments.repository.EnrollmentRepository;
import tech.impulso.enrollments.repository.LessonCompletionRepository;
import tech.impulso.lessons.entity.Lesson;
import tech.impulso.lessons.repository.LessonRepository;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Servicio que coordina la inscripción de estudiantes a cursos y el
 * seguimiento del progreso (RF-011).
 *
 * <p>Se apoya en dos entidades: {@link Enrollment} para el vínculo
 * estudiante-curso y {@link LessonCompletion} para las lecciones
 * completadas. El estado agregado de la inscripción y el porcentaje de
 * avance se recalculan cada vez que el estudiante marca una lección.</p>
 */
@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonCompletionRepository completionRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final CurrentUserService currentUserService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             LessonCompletionRepository completionRepository,
                             LessonRepository lessonRepository,
                             CourseRepository courseRepository,
                             CurrentUserService currentUserService) {
        this.enrollmentRepository = enrollmentRepository;
        this.completionRepository = completionRepository;
        this.lessonRepository = lessonRepository;
        this.courseRepository = courseRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Inscribe al usuario autenticado en el curso indicado.
     *
     * <p>La operación es idempotente: si el estudiante ya se encuentra
     * inscrito, se devuelve la inscripción existente sin duplicarla.</p>
     *
     * @param courseId identificador del curso.
     * @return la inscripción resultante.
     */
    @Transactional
    public EnrollmentResponse enroll(Long courseId) {
        User user = currentUserService.requireAuthenticatedUser();
        Course course = loadCourse(courseId);

        if (course.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.CONFLICT, "No es posible inscribirse en un curso que no está publicado.");
        }

        return enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .map(EnrollmentResponse::from)
                .orElseGet(() -> {
                    Enrollment enrollment = new Enrollment();
                    enrollment.setUser(user);
                    enrollment.setCourse(course);
                    enrollment.setStatus(EnrollmentStatus.INSCRITO);
                    return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
                });
    }

    /**
     * Lista las inscripciones del usuario autenticado.
     *
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con las inscripciones del estudiante.
     */
    @Transactional(readOnly = true)
    public PagedResponse<EnrollmentResponse> listMyEnrollments(Pageable pageable) {
        User user = currentUserService.requireAuthenticatedUser();
        Page<Enrollment> page = enrollmentRepository.findByUserId(user.getId(), pageable);
        return PagedResponse.from(page, EnrollmentResponse::from);
    }

    /**
     * Marca una lección como completada por el usuario autenticado y
     * recalcula el estado de avance de la inscripción correspondiente.
     *
     * @param lessonId identificador de la lección.
     * @return el progreso actualizado del curso al que pertenece la lección.
     */
    @Transactional
    public CourseProgressResponse completeLesson(Long lessonId) {
        User user = currentUserService.requireAuthenticatedUser();
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La lección indicada no existe."));

        if (lesson.getStatus() != ContentStatus.PUBLICADO
                || lesson.getModule().getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.CONFLICT, "La lección no está disponible para completar.");
        }

        Course course = lesson.getModule().getCourse();
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), course.getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT,
                        "Debe inscribirse en el curso antes de completar sus lecciones."));

        if (!completionRepository.existsByUserIdAndLessonId(user.getId(), lessonId)) {
            LessonCompletion completion = new LessonCompletion();
            completion.setUser(user);
            completion.setLesson(lesson);
            completionRepository.save(completion);
        }

        refreshEnrollmentStatus(enrollment, course.getId());
        enrollmentRepository.save(enrollment);

        return buildProgress(enrollment, course);
    }

    /**
     * Consulta el progreso del usuario autenticado en un curso concreto.
     *
     * @param courseId identificador del curso.
     * @return progreso agregado del estudiante en el curso.
     */
    @Transactional(readOnly = true)
    public CourseProgressResponse getProgress(Long courseId) {
        User user = currentUserService.requireAuthenticatedUser();
        Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "No existe una inscripción para este curso."));
        return buildProgress(enrollment, enrollment.getCourse());
    }

    /**
     * Recalcula el estado de la inscripción a partir de las lecciones
     * completadas por el estudiante.
     *
     * @param enrollment inscripción a actualizar.
     * @param courseId   identificador del curso asociado.
     */
    private void refreshEnrollmentStatus(Enrollment enrollment, Long courseId) {
        long mandatoryTotal = lessonRepository.countMandatoryPublishedInCourse(courseId);
        long mandatoryCompleted = completionRepository.countMandatoryCompletedInCourse(enrollment.getUser().getId(), courseId);

        enrollment.setLastAccessedAt(OffsetDateTime.now(ZoneOffset.UTC));

        if (mandatoryTotal > 0 && mandatoryCompleted >= mandatoryTotal) {
            if (enrollment.getStatus() != EnrollmentStatus.COMPLETADO) {
                enrollment.setStatus(EnrollmentStatus.COMPLETADO);
                enrollment.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
            }
        } else if (enrollment.getStatus() == EnrollmentStatus.INSCRITO && mandatoryCompleted > 0) {
            enrollment.setStatus(EnrollmentStatus.EN_PROGRESO);
        }
    }

    /**
     * Construye la representación del progreso a partir del estado
     * actual de la inscripción y de las lecciones completadas.
     *
     * @param enrollment inscripción del estudiante.
     * @param course     curso asociado.
     * @return progreso agregado.
     */
    private CourseProgressResponse buildProgress(Enrollment enrollment, Course course) {
        Long userId = enrollment.getUser().getId();
        long mandatoryTotal = lessonRepository.countMandatoryPublishedInCourse(course.getId());
        long mandatoryCompleted = completionRepository.countMandatoryCompletedInCourse(userId, course.getId());
        int percentage = mandatoryTotal == 0
                ? 0
                : (int) Math.round((mandatoryCompleted * 100.0) / mandatoryTotal);
        List<Long> completedLessonIds = completionRepository.findCompletedLessonIdsInCourse(userId, course.getId());

        return new CourseProgressResponse(
                course.getId(),
                course.getName(),
                enrollment.getStatus(),
                mandatoryTotal,
                mandatoryCompleted,
                percentage,
                completedLessonIds,
                enrollment.getStartedAt(),
                enrollment.getCompletedAt(),
                enrollment.getLastAccessedAt()
        );
    }

    private Course loadCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El curso indicado no existe."));
    }
}
