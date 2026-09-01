package tech.impulso.auth.dto;

import java.time.OffsetDateTime;

/**
 * Respuesta emitida tras un inicio de sesión exitoso.
 *
 * @param accessToken token JWT que debe enviarse en el encabezado
 *                    {@code Authorization} de las peticiones posteriores.
 * @param tokenType   tipo del token, siempre {@code Bearer}.
 * @param expiresAt   momento en que el token dejará de ser válido.
 * @param user        información pública del usuario autenticado.
 */
public record AuthResponse(
        String accessToken,
        String tokenType,
        OffsetDateTime expiresAt,
        UserResponse user
) {
}
