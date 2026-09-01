package tech.impulso.enrollments.dto;

import tech.impulso.enrollments.entity.EnrollmentStatus;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Progreso agregado de un estudiante en un curso.
 *
 * @param courseId                   identificador del curso.
 * @param courseName                 nombre del curso.
 * @param status                     estado agregado de la inscripción.
 * @param mandatoryLessonsTotal      cantidad total de lecciones
 *                                   obligatorias publicadas del curso.
 * @param mandatoryLessonsCompleted  cantidad de lecciones obligatorias
 *                                   completadas por el estudiante.
 * @param completionPercentage       porcentaje de avance sobre las
 *                                   lecciones obligatorias (0 a 100).
 * @param completedLessonIds         identificadores de las lecciones
 *                                   completadas por el estudiante
 *                                   (incluye las opcionales).
 * @param startedAt                  fecha en que el estudiante inició el curso.
 * @param completedAt                fecha en que finalizó el curso, cuando aplica.
 * @param lastAccessedAt             fecha del último avance registrado.
 */
public record CourseProgressResponse(
        Long courseId,
        String courseName,
        EnrollmentStatus status,
        long mandatoryLessonsTotal,
        long mandatoryLessonsCompleted,
        int completionPercentage,
        List<Long> completedLessonIds,
        OffsetDateTime startedAt,
        OffsetDateTime completedAt,
        OffsetDateTime lastAccessedAt
) {
}
