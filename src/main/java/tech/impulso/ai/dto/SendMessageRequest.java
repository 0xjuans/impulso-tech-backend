package tech.impulso.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para enviar un mensaje del usuario a la mascota IA
 * (RF-017).
 *
 * @param message contenido del mensaje. Se limita a 4000 caracteres
 *                para acotar el tamaño del prompt enviado al proveedor.
 */
public record SendMessageRequest(
        @NotBlank @Size(max = 4000) String message
) {
}
