package tech.impulso.statistics.dto;

import java.util.List;

/**
 * Estadísticas agregadas para el instructor (RF-058).
 *
 * <p>Se calcula únicamente sobre los cursos del instructor autenticado.
 * Incluye la evolución de inscripciones, el detalle de finalización por
 * curso y un valor global de rendimiento.</p>
 *
 * @param overallCompletionRate porcentaje de finalización global sobre
 *                              todos sus cursos, en el rango [0, 100].
 * @param enrollmentsByDay      serie temporal de inscripciones en sus
 *                              cursos.
 * @param courses               detalle por curso: inscripciones y
 *                              porcentaje de finalización.
 * @param windowDays            tamaño en días de la ventana temporal.
 */
public record InstructorStatisticsResponse(
        double overallCompletionRate,
        List<TimeSeriesPoint> enrollmentsByDay,
        List<CourseEnrollmentStat> courses,
        int windowDays
) {
}
