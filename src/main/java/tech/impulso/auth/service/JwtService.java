package tech.impulso.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.impulso.users.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Servicio encargado de generar y validar los tokens JWT utilizados por
 * la plataforma para autenticar las peticiones a la API.
 *
 * <p>Los tokens contienen el correo del usuario como sujeto, su rol como
 * claim personalizado y un identificador único ({@code jti}) que permite
 * invalidarlos individualmente mediante la lista negra en Redis cuando el
 * usuario cierra sesión.</p>
 */
@Service
public class JwtService {

    private static final String ROLE_CLAIM = "role";

    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    /**
     * Genera un nuevo token de acceso para el usuario autenticado.
     *
     * @param user   usuario autenticado.
     * @param tokenId identificador único del token, utilizado para poder
     *                invalidarlo posteriormente si el usuario cierra sesión.
     * @return el token firmado listo para ser devuelto al cliente.
     */
    public String generateToken(User user, String tokenId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiration);
        return Jwts.builder()
                .id(tokenId)
                .subject(user.getEmail())
                .claim(ROLE_CLAIM, user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Valida la firma y la vigencia del token y devuelve sus claims.
     *
     * @param token token recibido en la petición.
     * @return los claims contenidos en el token.
     * @throws JwtException si el token es inválido o ha expirado.
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Duración configurada de los tokens de acceso.
     *
     * @return la duración expresada como {@link Duration}.
     */
    public Duration getExpiration() {
        return expiration;
    }
}
