package tech.impulso.mascotcustomization.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.impulso.mascotcustomization.entity.MascotItemSlot;
import tech.impulso.mascotcustomization.entity.MascotUnlockType;

/**
 * Datos necesarios para crear o actualizar un ítem del catálogo de la
 * mascota (RF-022, RF-050, RF-052).
 *
 * @param code         código único del ítem, en {@code kebab-case}.
 * @param name         nombre visible.
 * @param description  descripción breve.
 * @param slot         ranura visual a la que se aplica.
 * @param imageUrl     URL o llave del recurso visual.
 * @param unlockType   criterio de desbloqueo.
 * @param unlockLevel  nivel mínimo requerido cuando aplica.
 * @param unlockXp     XP mínimo requerido cuando aplica.
 * @param unlockBadgeId identificador de la insignia requerida cuando aplica.
 * @param active       indica si el ítem está activo en el catálogo.
 */
public record MascotItemRequest(
        @NotBlank @Size(max = 60) String code,
        @NotBlank @Size(max = 120) String name,
        String description,
        @NotNull MascotItemSlot slot,
        @Size(max = 500) String imageUrl,
        @NotNull MascotUnlockType unlockType,
        @Min(1) Integer unlockLevel,
        @Min(0) Integer unlockXp,
        Long unlockBadgeId,
        boolean active
) {
}
