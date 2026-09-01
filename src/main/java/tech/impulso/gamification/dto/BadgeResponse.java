package tech.impulso.gamification.dto;

import tech.impulso.gamification.entity.Badge;
import tech.impulso.gamification.entity.BadgeCategory;
import tech.impulso.gamification.entity.BadgeRarity;
import tech.impulso.gamification.entity.BadgeTrigger;

/**
 * Representación pública de una insignia del catálogo.
 *
 * @param id           identificador interno.
 * @param code         código estable.
 * @param name         nombre visible.
 * @param description  descripción orientada al estudiante.
 * @param iconUrl      URL pública del ícono.
 * @param category     categoría temática.
 * @param rarity       rareza asignada.
 * @param triggerType  disparador automático.
 * @param triggerValue umbral asociado al disparador.
 */
public record BadgeResponse(
        Long id,
        String code,
        String name,
        String description,
        String iconUrl,
        BadgeCategory category,
        BadgeRarity rarity,
        BadgeTrigger triggerType,
        int triggerValue
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param badge entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static BadgeResponse from(Badge badge) {
        return new BadgeResponse(
                badge.getId(),
                badge.getCode(),
                badge.getName(),
                badge.getDescription(),
                badge.getIconUrl(),
                badge.getCategory(),
                badge.getRarity(),
                badge.getTriggerType(),
                badge.getTriggerValue()
        );
    }
}
