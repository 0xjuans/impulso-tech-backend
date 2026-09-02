package tech.impulso.support.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.impulso.support.entity.SupportTicketType;

/**
 * Datos requeridos para crear un nuevo ticket de soporte.
 *
 * @param type        tipo del ticket.
 * @param title       título breve del problema.
 * @param description descripción detallada.
 * @param relatedId   identificador del recurso relacionado, cuando el
 *                    tipo lo requiera.
 */
public record CreateTicketRequest(
        @NotNull(message = "El tipo del ticket es obligatorio.")
        SupportTicketType type,

        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 200)
        String title,

        @NotBlank(message = "La descripción es obligatoria.")
        String description,

        Long relatedId
) {
}
