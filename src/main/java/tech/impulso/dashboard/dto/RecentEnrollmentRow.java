package tech.impulso.dashboard.dto;

import java.time.OffsetDateTime;

/**
 * Fila del feed "Últimas inscripciones" del panel del instructor
 * (RF-032). Cada fila representa una inscripción reciente e incluye
 * lo mínimo para que el instructor identifique curso, estudiante y
 * momento sin cargar detalles secundarios.
 */
public record RecentEnrollmentRow(
        Long enrollmentId,
        Long courseId,
        String courseName,
        Long studentId,
        String studentName,
        String status,
        OffsetDateTime startedAt
) {
}
