package tech.impulso.messaging.dto;

import tech.impulso.messaging.entity.MessageConversation;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;

/**
 * Representación pública de una conversación (RF-061).
 *
 * <p>Los datos del otro participante y el resumen del último mensaje
 * se incluyen para facilitar el renderizado de la lista de
 * conversaciones sin realizar peticiones adicionales.</p>
 *
 * @param id                identificador de la conversación.
 * @param otherUserId       identificador del otro participante.
 * @param otherUsername     nombre de usuario del otro participante.
 * @param otherFullName     nombre completo del otro participante.
 * @param otherPhotoUrl     URL de la foto del otro participante.
 * @param lastMessagePreview vista breve del último mensaje intercambiado.
 * @param lastMessageAt     fecha del último mensaje.
 * @param unreadCount       cantidad de mensajes no leídos por el usuario
 *                          autenticado.
 */
public record ConversationResponse(
        Long id,
        Long otherUserId,
        String otherUsername,
        String otherFullName,
        String otherPhotoUrl,
        String lastMessagePreview,
        OffsetDateTime lastMessageAt,
        long unreadCount
) {

    public static ConversationResponse of(MessageConversation conversation,
                                          Long viewerId,
                                          String lastMessagePreview,
                                          long unreadCount) {
        User other = conversation.otherParticipant(viewerId);
        String fullName = ("%s %s".formatted(
                other.getFirstName() == null ? "" : other.getFirstName(),
                other.getLastName() == null ? "" : other.getLastName())).trim();
        return new ConversationResponse(
                conversation.getId(),
                other.getId(),
                other.getUsername(),
                fullName.isEmpty() ? null : fullName,
                other.getProfilePhotoUrl(),
                lastMessagePreview,
                conversation.getLastMessageAt(),
                unreadCount
        );
    }
}
