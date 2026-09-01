package tech.impulso.gamification.dto;

import tech.impulso.gamification.entity.XpEvent;
import tech.impulso.gamification.entity.XpSource;

import java.time.OffsetDateTime;

/**
 * Representación pública de un evento del historial de XP.
 *
 * @param id         identificador interno del evento.
 * @param sourceType fuente que originó el otorgamiento.
 * @param sourceId   identificador del recurso asociado.
 * @param xpAwarded  cantidad de XP otorgada.
 * @param createdAt  fecha y hora del otorgamiento.
 */
public record XpEventResponse(
        Long id,
        XpSource sourceType,
        Long sourceId,
        int xpAwarded,
        OffsetDateTime createdAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param event evento del historial.
     * @return DTO listo para devolver desde la API.
     */
    public static XpEventResponse from(XpEvent event) {
        return new XpEventResponse(
                event.getId(),
                event.getSourceType(),
                event.getSourceId(),
                event.getXpAwarded(),
                event.getCreatedAt()
        );
    }
}
