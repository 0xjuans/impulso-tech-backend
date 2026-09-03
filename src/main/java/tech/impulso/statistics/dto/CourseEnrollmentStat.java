package tech.impulso.statistics.dto;

/**
 * Estadística de inscripciones para un curso (RF-058).
 *
 * @param courseId      identificador del curso.
 * @param courseName    nombre visible del curso.
 * @param enrollments   cantidad total de inscripciones registradas.
 * @param completed     cantidad de inscripciones en estado
 *                      {@code COMPLETADO}. Es {@code null} cuando la
 *                      estadística no contempla el detalle de estado.
 * @param completionRate porcentaje de finalización sobre el total, en el
 *                      rango [0, 100]. Es {@code null} cuando no se
 *                      dispone del detalle de completados.
 */
public record CourseEnrollmentStat(
        Long courseId,
        String courseName,
        long enrollments,
        Long completed,
        Double completionRate
) {
}
