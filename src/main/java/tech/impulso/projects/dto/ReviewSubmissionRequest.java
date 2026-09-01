package tech.impulso.projects.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tech.impulso.projects.entity.ProjectSubmissionStatus;

/**
 * Solicitud del instructor para revisar una entrega de proyecto.
 *
 * @param status   nuevo estado ({@code APROBADA}, {@code CORRECCION_SOLICITADA}
 *                 o {@code RECHAZADA}).
 * @param grade    calificación otorgada (0 a 100); opcional cuando el
 *                 estado no requiere calificación.
 * @param feedback retroalimentación textual para el estudiante.
 */
public record ReviewSubmissionRequest(
        @NotNull(message = "El estado de la revisión es obligatorio.")
        ProjectSubmissionStatus status,

        @Min(value = 0, message = "La calificación no puede ser negativa.")
        @Max(value = 100, message = "La calificación no puede superar 100.")
        Integer grade,

        String feedback
) {
}
