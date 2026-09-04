package tech.impulso.messaging.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Solicitud para abrir una conversación con otro usuario (RF-061).
 *
 * @param recipientId identificador del usuario destinatario.
 */
public record StartConversationRequest(
        @NotNull Long recipientId
) {
}
