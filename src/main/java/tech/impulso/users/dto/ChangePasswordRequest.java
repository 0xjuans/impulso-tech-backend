package tech.impulso.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para cambiar la contraseña del usuario autenticado.
 *
 * <p>Requiere la contraseña actual como control de seguridad, para evitar
 * que un token robado permita modificar la contraseña sin conocer las
 * credenciales originales del usuario.</p>
 *
 * @param currentPassword contraseña actual del usuario.
 * @param newPassword     nueva contraseña que reemplazará a la anterior.
 */
public record ChangePasswordRequest(
        @NotBlank(message = "La contraseña actual es obligatoria.")
        String currentPassword,

        @NotBlank(message = "La nueva contraseña es obligatoria.")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "La contraseña debe incluir al menos una letra y un número.")
        String newPassword
) {
}
