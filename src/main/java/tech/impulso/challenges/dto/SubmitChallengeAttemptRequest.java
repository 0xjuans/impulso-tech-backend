package tech.impulso.challenges.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos requeridos para enviar un intento de solución a un reto.
 *
 * @param language lenguaje utilizado; debe estar entre los permitidos
 *                 por el reto.
 * @param code     código fuente enviado por el estudiante.
 */
public record SubmitChallengeAttemptRequest(
        @NotBlank(message = "El lenguaje es obligatorio.")
        @Size(max = 60)
        String language,

        @NotBlank(message = "El código enviado es obligatorio.")
        String code
) {
}
