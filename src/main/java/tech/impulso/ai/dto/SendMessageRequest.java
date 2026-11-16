package tech.impulso.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para enviar un mensaje del usuario a la mascota IA
 * (RF-017).
 *
 * @param message contenido del mensaje. Se limita a 2000 caracteres
 *                (~500 tokens) para acotar el tamaño del prompt enviado
 *                al proveedor y proteger el presupuesto de tokens.
 */
public record SendMessageRequest(
        @NotBlank @Size(max = 2000) String message
) {
}
