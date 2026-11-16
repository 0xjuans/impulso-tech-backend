package tech.impulso.messaging.dto;

import tech.impulso.messaging.entity.Message;

import java.time.OffsetDateTime;

/**
 * Representación pública de un mensaje (RF-061).
 *
 * @param id             identificador del mensaje.
 * @param conversationId conversación a la que pertenece el mensaje.
 * @param senderId       identificador del emisor.
 * @param content        contenido literal.
 * @param sentAt         fecha en que fue enviado.
 * @param readAt         fecha en la que el receptor lo marcó como leído.
 */
public record MessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String content,
        OffsetDateTime sentAt,
        OffsetDateTime readAt
) {

    public static MessageResponse from(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getConversation() != null ? message.getConversation().getId() : null,
                message.getSender().getId(),
                message.getContent(),
                message.getSentAt(),
                message.getReadAt()
        );
    }
}
