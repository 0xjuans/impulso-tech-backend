package tech.impulso.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para establecer una nueva contraseña utilizando un enlace de
 * recuperación previamente emitido.
 *
 * @param token       valor del token recibido por correo.
 * @param newPassword nueva contraseña que reemplazará a la anterior.
 */
public record ResetPasswordRequest(
        @NotBlank(message = "El token es obligatorio.")
        String token,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "La contraseña debe incluir al menos una letra y un número.")
        String newPassword
) {
}
