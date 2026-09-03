package tech.impulso.ai.dto;

import tech.impulso.ai.entity.AiMessage;
import tech.impulso.ai.entity.AiMessageRole;

import java.time.OffsetDateTime;

/**
 * Representación pública de un mensaje de la conversación con la
 * mascota IA (RF-017).
 *
 * <p>Los mensajes con rol {@link AiMessageRole#SYSTEM} no se exponen al
 * cliente; el servicio los filtra antes de construir la respuesta.</p>
 *
 * @param id         identificador interno del mensaje.
 * @param role       rol del emisor.
 * @param content    contenido literal del mensaje.
 * @param createdAt  fecha en que se registró el mensaje.
 */
public record MessageResponse(
        Long id,
        AiMessageRole role,
        String content,
        OffsetDateTime createdAt
) {

    public static MessageResponse from(AiMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getRole(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
