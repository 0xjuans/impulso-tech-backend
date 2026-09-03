package tech.impulso.mascotcustomization.dto;

import tech.impulso.mascotcustomization.entity.MascotCustomization;
import tech.impulso.mascotcustomization.entity.MascotItem;

/**
 * Personalización actual de la mascota (RF-052).
 *
 * <p>Cada ranura devuelve una versión reducida del ítem equipado o
 * {@code null} cuando el usuario no ha personalizado esa ranura.</p>
 */
public record CustomizationResponse(
        EquippedItem skin,
        EquippedItem hat,
        EquippedItem accessory,
        EquippedItem background
) {

    public static CustomizationResponse from(MascotCustomization customization) {
        if (customization == null) {
            return new CustomizationResponse(null, null, null, null);
        }
        return new CustomizationResponse(
                EquippedItem.from(customization.getSkin()),
                EquippedItem.from(customization.getHat()),
                EquippedItem.from(customization.getAccessory()),
                EquippedItem.from(customization.getBackground())
        );
    }

    /**
     * Vista compacta de un ítem equipado.
     */
    public record EquippedItem(Long id, String code, String name, String imageUrl) {

        public static EquippedItem from(MascotItem item) {
            if (item == null) {
                return null;
            }
            return new EquippedItem(item.getId(), item.getCode(), item.getName(), item.getImageUrl());
        }
    }
}
