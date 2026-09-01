package tech.impulso.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Credenciales suministradas por el usuario para iniciar sesión (RF-002).
 *
 * @param email    correo electrónico registrado.
 * @param password contraseña en texto plano; se valida contra el hash
 *                 almacenado.
 */
public record LoginRequest(
        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "El correo electrónico no tiene un formato válido.")
        String email,

        @NotBlank(message = "La contraseña es obligatoria.")
        String password
) {
}
