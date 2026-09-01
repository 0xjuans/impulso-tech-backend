package tech.impulso.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Solicitud de inicio de sesión mediante Google (RF-004).
 *
 * <p>El frontend obtiene el {@code idToken} desde Google Identity
 * Services y lo envía al backend, que lo verifica contra las claves
 * públicas de Google antes de emitir el JWT de sesión propio.</p>
 *
 * @param idToken token de identidad emitido por Google (JWT firmado).
 */
public record GoogleLoginRequest(
        @NotBlank(message = "El idToken de Google es obligatorio.")
        String idToken
) {
}
