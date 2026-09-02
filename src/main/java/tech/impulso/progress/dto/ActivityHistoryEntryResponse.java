package tech.impulso.progress.dto;

import java.time.OffsetDateTime;

/**
 * Entrada individual del historial de actividad del estudiante
 * (RF-028).
 *
 * <p>Cada entrada unifica en un formato común eventos provenientes de
 * distintas fuentes (XP otorgada, insignias obtenidas, etc.) para
 * facilitar la presentación cronológica.</p>
 *
 * @param kind        tipo de la entrada (por ejemplo, {@code XP_EVENT} o {@code BADGE_AWARDED}).
 * @param code        código específico del evento (por ejemplo, {@code LESSON_COMPLETED}).
 * @param title       título orientado al usuario.
 * @param description descripción legible del evento.
 * @param xpAwarded   XP asociada al evento, cuando aplique.
 * @param relatedId   identificador del recurso relacionado, cuando aplique.
 * @param createdAt   momento en que ocurrió el evento.
 */
public record ActivityHistoryEntryResponse(
        String kind,
        String code,
        String title,
        String description,
        Integer xpAwarded,
        Long relatedId,
        OffsetDateTime createdAt
) {
}
