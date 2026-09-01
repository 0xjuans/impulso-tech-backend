package tech.impulso.users.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos que el usuario puede modificar en su propio perfil (RF-005).
 *
 * <p>Todos los campos son opcionales: si un valor llega como {@code null}
 * el campo no se actualiza. Los cambios sobre el rol o el estado de la
 * cuenta no forman parte de este flujo y solo pueden ser realizados por
 * el administrador desde los endpoints correspondientes.</p>
 *
 * @param username        nuevo nombre de usuario público.
 * @param firstName       nuevo nombre real.
 * @param lastName        nuevo apellido real.
 * @param profilePhotoUrl nueva URL pública de la foto de perfil.
 * @param showInRanking   preferencia de visibilidad pública en los rankings.
 */
public record UpdateProfileRequest(
        @Size(min = 3, max = 60, message = "El nombre de usuario debe tener entre 3 y 60 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos.")
        String username,

        @Size(min = 1, max = 80, message = "El nombre debe tener entre 1 y 80 caracteres.")
        String firstName,

        @Size(min = 1, max = 80, message = "El apellido debe tener entre 1 y 80 caracteres.")
        String lastName,

        @Size(max = 500, message = "La URL de la foto de perfil no puede superar los 500 caracteres.")
        String profilePhotoUrl,

        Boolean showInRanking
) {
}
