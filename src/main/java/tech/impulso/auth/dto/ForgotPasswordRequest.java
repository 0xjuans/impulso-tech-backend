package tech.impulso.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud de recuperación de contraseña (RF-003 / RF-054). El sistema
 * responderá siempre con éxito para no revelar si el correo se encuentra
 * o no registrado.
 *
 * @param email correo electrónico asociado a la cuenta.
 */
public record ForgotPasswordRequest(
        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "El correo electrónico no tiene un formato válido.")
        String email
) {
}
