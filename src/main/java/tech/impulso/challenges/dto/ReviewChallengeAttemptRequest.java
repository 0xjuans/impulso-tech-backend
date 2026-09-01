package tech.impulso.challenges.dto;

import jakarta.validation.constraints.NotNull;
import tech.impulso.challenges.entity.ChallengeAttemptStatus;

/**
 * Solicitud del instructor para revisar un intento de reto.
 *
 * @param status   nuevo estado ({@code APROBADO} o {@code RECHAZADO}).
 * @param feedback retroalimentación textual para el estudiante.
 */
public record ReviewChallengeAttemptRequest(
        @NotNull(message = "El estado de la revisión es obligatorio.")
        ChallengeAttemptStatus status,

        String feedback
) {
}
