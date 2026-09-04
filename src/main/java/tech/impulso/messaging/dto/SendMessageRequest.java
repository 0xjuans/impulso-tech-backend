package tech.impulso.messaging.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para enviar un mensaje dentro de una conversación
 * (RF-061).
 *
 * @param content texto del mensaje; se limita a 4000 caracteres para
 *                acotar el tamaño y evitar abuso.
 */
public record SendMessageRequest(
        @NotBlank @Size(max = 4000) String content
) {
}
