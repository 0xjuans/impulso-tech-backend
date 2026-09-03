package tech.impulso.mascotcustomization.dto;

import tech.impulso.mascotcustomization.entity.MascotItem;
import tech.impulso.mascotcustomization.entity.MascotItemSlot;
import tech.impulso.mascotcustomization.entity.MascotUnlockType;

/**
 * Ítem del catálogo con el estado del usuario autenticado (RF-052).
 *
 * @param id            identificador del ítem.
 * @param code          código único.
 * @param name          nombre visible.
 * @param description   descripción breve.
 * @param slot          ranura visual.
 * @param imageUrl      URL o llave del recurso visual.
 * @param unlockType    criterio de desbloqueo.
 * @param unlockLevel   nivel mínimo requerido cuando aplica.
 * @param unlockXp      XP mínimo requerido cuando aplica.
 * @param unlockBadgeId insignia requerida cuando aplica.
 * @param unlocked      indica si el usuario ya tiene el ítem desbloqueado.
 * @param equipped      indica si el usuario tiene el ítem equipado en su
 *                      personalización actual.
 */
public record CatalogItemResponse(
        Long id,
        String code,
        String name,
        String description,
        MascotItemSlot slot,
        String imageUrl,
        MascotUnlockType unlockType,
        Integer unlockLevel,
        Integer unlockXp,
        Long unlockBadgeId,
        boolean unlocked,
        boolean equipped
) {

    public static CatalogItemResponse of(MascotItem item, boolean unlocked, boolean equipped) {
        return new CatalogItemResponse(
                item.getId(),
                item.getCode(),
                item.getName(),
                item.getDescription(),
                item.getSlot(),
                item.getImageUrl(),
                item.getUnlockType(),
                item.getUnlockLevel(),
                item.getUnlockXp(),
                item.getUnlockBadge() == null ? null : item.getUnlockBadge().getId(),
                unlocked,
                equipped
        );
    }
}
