package tech.impulso.ai.dto;

import jakarta.validation.constraints.Size;
import tech.impulso.ai.entity.AiContextType;

/**
 * Solicitud para iniciar una nueva conversación con la mascota IA
 * (RF-017).
 *
 * @param title       título opcional; el servicio genera uno automático
 *                    cuando llega vacío.
 * @param contextType tipo de contexto de aprendizaje al que anclar la
 *                    conversación. {@code null} equivale a
 *                    {@link AiContextType#GENERAL}.
 * @param contextId   identificador del elemento de contexto. Es
 *                    obligatorio para todo {@code contextType} distinto
 *                    de {@code GENERAL}.
 */
public record StartConversationRequest(
        @Size(max = 200) String title,
        AiContextType contextType,
        Long contextId
) {
}
