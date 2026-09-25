package tech.impulso.certificates.dto;

import java.time.OffsetDateTime;

/**
 * Curso que el usuario ha completado, para el cual el curso emite
 * certificado, pero para el que aún no se ha emitido uno.
 *
 * <p>Se muestra en la sección "Pendientes por reclamar" de la página de
 * certificados. El estudiante puede solicitar la emisión con un click
 * (RF-047).</p>
 *
 * @param courseId    identificador del curso.
 * @param courseName  nombre del curso.
 * @param completedAt fecha en que el estudiante terminó el curso.
 */
public record PendingCertificateResponse(
        Long courseId,
        String courseName,
        OffsetDateTime completedAt
) {
}
