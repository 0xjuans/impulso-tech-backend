package tech.impulso.statistics.dto;

import java.util.List;
import java.util.Map;

/**
 * Estadísticas agregadas de plataforma expuestas al administrador
 * (RF-058).
 *
 * <p>Incluye distribuciones (usuarios por rol, cursos por dificultad),
 * indicadores calculados (tasa global de finalización) y series
 * temporales para representar la evolución en los últimos días.</p>
 *
 * @param usersByRole         cantidad de usuarios agrupada por rol.
 * @param coursesByDifficulty cantidad de cursos por nivel de dificultad.
 * @param overallCompletionRate porcentaje global de inscripciones
 *                            completadas, en el rango [0, 100].
 * @param registrationsByDay  serie temporal de registros de usuarios.
 * @param enrollmentsByDay    serie temporal de inscripciones.
 * @param topCourses          cursos con más inscripciones.
 * @param windowDays          tamaño en días de las ventanas temporales.
 */
public record PlatformStatisticsResponse(
        Map<String, Long> usersByRole,
        Map<String, Long> coursesByDifficulty,
        double overallCompletionRate,
        List<TimeSeriesPoint> registrationsByDay,
        List<TimeSeriesPoint> enrollmentsByDay,
        List<CourseEnrollmentStat> topCourses,
        int windowDays
) {
}
