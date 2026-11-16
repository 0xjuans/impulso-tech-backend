package tech.impulso.dashboard.dto;

/**
 * Fila de la tabla "Top cursos" del panel del instructor y del
 * administrador (RF-032 / RF-033).
 *
 * <p>Refleja la agregación de inscripciones por curso: total y
 * completadas. La tasa de finalización se calcula en el frontend a
 * partir de estos dos valores para evitar duplicar la lógica.</p>
 *
 * @param courseId              identificador del curso.
 * @param name                  nombre visible del curso.
 * @param totalEnrollments      cantidad total de inscripciones.
 * @param completedEnrollments  cantidad en estado {@code COMPLETADO}.
 */
public record TopCourseRow(
        Long courseId,
        String name,
        long totalEnrollments,
        long completedEnrollments
) {
}
