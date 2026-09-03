package tech.impulso.ai.dto;

import tech.impulso.ai.entity.AiContextType;
import tech.impulso.ai.entity.AiConversation;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Representación pública de una conversación con la mascota IA
 * (RF-017).
 *
 * <p>{@code messages} es opcional: se llena únicamente cuando se
 * solicita el detalle o cuando el cliente pide la conversación completa
 * tras enviar un mensaje.</p>
 *
 * @param id          identificador de la conversación.
 * @param title       título visible.
 * @param contextType tipo de contexto asociado.
 * @param contextId   identificador del elemento de contexto.
 * @param createdAt   fecha de creación.
 * @param updatedAt   fecha de la última interacción.
 * @param closedAt    fecha de cierre (o {@code null} si sigue activa).
 * @param messages    mensajes visibles al usuario; puede ser {@code null}.
 */
public record ConversationResponse(
        Long id,
        String title,
        AiContextType contextType,
        Long contextId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        OffsetDateTime closedAt,
        List<MessageResponse> messages
) {

    public static ConversationResponse summary(AiConversation conversation) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getContextType(),
                conversation.getContextId(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                conversation.getClosedAt(),
                null
        );
    }

    public static ConversationResponse withMessages(AiConversation conversation,
                                                    List<MessageResponse> messages) {
        return new ConversationResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getContextType(),
                conversation.getContextId(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                conversation.getClosedAt(),
                messages
        );
    }
}
