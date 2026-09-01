package tech.impulso.auth.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Servicio encargado de mantener la lista negra de tokens JWT invalidados.
 *
 * <p>Al operar de manera stateless con JWT, el cierre de sesión no puede
 * limitarse a eliminar una sesión del servidor: el token seguiría siendo
 * válido hasta su expiración natural. Para cumplir el RF-007 (cierre de
 * sesión) y el RF-055 (gestión de sesiones) se utiliza Redis para marcar
 * los identificadores de token invalidados hasta que su vigencia expira,
 * momento en el cual Redis los elimina automáticamente.</p>
 */
@Service
public class TokenBlacklistService {

    /** Prefijo de las claves utilizadas en Redis para identificar la lista negra. */
    private static final String KEY_PREFIX = "auth:blacklist:";

    private final StringRedisTemplate redisTemplate;

    public TokenBlacklistService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Marca un identificador de token como invalidado.
     *
     * @param tokenId    identificador único del token ({@code jti}).
     * @param ttlSeconds cantidad de segundos que debe permanecer marcado,
     *                   equivalente al tiempo restante de vida del token.
     */
    public void blacklist(String tokenId, long ttlSeconds) {
        if (ttlSeconds <= 0) {
            return;
        }
        redisTemplate.opsForValue().set(KEY_PREFIX + tokenId, "revoked", Duration.ofSeconds(ttlSeconds));
    }

    /**
     * Indica si un token ha sido invalidado.
     *
     * @param tokenId identificador único del token.
     * @return {@code true} cuando el token se encuentra en la lista negra.
     */
    public boolean isBlacklisted(String tokenId) {
        Boolean present = redisTemplate.hasKey(KEY_PREFIX + tokenId);
        return Boolean.TRUE.equals(present);
    }
}
