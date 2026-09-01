package tech.impulso.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos suministrados por el cliente para registrar un nuevo usuario en
 * Impulso Tech (RF-001).
 *
 * <p>La contraseña debe cumplir con los criterios mínimos de seguridad
 * definidos por la plataforma: al menos 8 caracteres, con presencia de
 * letras y números.</p>
 *
 * @param email     correo electrónico único del usuario.
 * @param username  nombre de usuario público.
 * @param password  contraseña en texto plano; se cifrará antes de persistir.
 * @param firstName nombre real del usuario.
 * @param lastName  apellido real del usuario.
 */
public record RegisterRequest(
        @NotBlank(message = "El correo electrónico es obligatorio.")
        @Email(message = "El correo electrónico no tiene un formato válido.")
        @Size(max = 255)
        String email,

        @NotBlank(message = "El nombre de usuario es obligatorio.")
        @Size(min = 3, max = 60, message = "El nombre de usuario debe tener entre 3 y 60 caracteres.")
        @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos.")
        String username,

        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres.")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
                message = "La contraseña debe incluir al menos una letra y un número.")
        String password,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 80)
        String firstName,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 80)
        String lastName
) {
}
