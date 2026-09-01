package tech.impulso.enrollments.dto;

import tech.impulso.enrollments.entity.Enrollment;
import tech.impulso.enrollments.entity.EnrollmentStatus;

import java.time.OffsetDateTime;

/**
 * Representación pública de una inscripción a un curso.
 *
 * @param id             identificador interno de la inscripción.
 * @param courseId       identificador del curso.
 * @param courseName     nombre del curso.
 * @param status         estado agregado del avance.
 * @param startedAt      fecha en que el estudiante inició el curso.
 * @param completedAt    fecha de finalización, cuando aplica.
 * @param lastAccessedAt fecha del último avance registrado.
 */
public record EnrollmentResponse(
        Long id,
        Long courseId,
        String courseName,
        EnrollmentStatus status,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        OffsetDateTime lastAccessedAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param enrollment entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static EnrollmentResponse from(Enrollment enrollment) {
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getCourse().getId(),
                enrollment.getCourse().getName(),
                enrollment.getStatus(),
                enrollment.getStartedAt(),
                enrollment.getCompletedAt(),
                enrollment.getLastAccessedAt()
        );
    }
}
