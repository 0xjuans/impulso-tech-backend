package tech.impulso.gamification.dto;

import tech.impulso.gamification.entity.UserBadge;

import java.time.OffsetDateTime;

/**
 * Representación pública de una insignia obtenida por un usuario.
 *
 * @param awardedAt fecha y hora en que se otorgó.
 * @param badge     detalle de la insignia asociada.
 */
public record UserBadgeResponse(
        OffsetDateTime awardedAt,
        BadgeResponse badge
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param userBadge entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static UserBadgeResponse from(UserBadge userBadge) {
        return new UserBadgeResponse(
                userBadge.getAwardedAt(),
                BadgeResponse.from(userBadge.getBadge())
        );
    }
}
