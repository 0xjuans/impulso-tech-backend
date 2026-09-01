package tech.impulso.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud del usuario para reportar contenido inapropiado en la
 * comunidad (RF-034, moderación).
 *
 * @param reason motivo del reporte suministrado por el usuario.
 */
public record CreateReportRequest(
        @NotBlank(message = "El motivo del reporte es obligatorio.")
        @Size(max = 1000, message = "El motivo no puede superar los 1000 caracteres.")
        String reason
) {
}
