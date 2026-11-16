package tech.impulso.common.ratelimit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Servicio de rate limiting basado en Redis (RF-034 / §34).
 *
 * <p>Implementa un contador de ventana fija: cada bucket + clientKey +
 * ventana temporal se traducen en una clave Redis con TTL igual al
 * tamaño de la ventana. La comprobación es atómica gracias al
 * comando {@code INCR} de Redis: la primera solicitud de la ventana
 * establece el {@code EXPIRE}, las posteriores solo incrementan el
 * contador.</p>
 *
 * <p>El diseño se prefiere sobre una ventana deslizante con
 * {@code ZSET} porque tiene menor coste operativo y es más fácil de
 * razonar; el error máximo posible es un ~2x en el borde entre
 * ventanas, aceptable para la clase de endpoints protegidos.</p>
 */
@Service
public class RateLimitService {

    private static final Logger log = LoggerFactory.getLogger(RateLimitService.class);

    private final StringRedisTemplate redisTemplate;

    public RateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Consulta el estado del contador para el cliente indicado y lo
     * incrementa. Devuelve la decisión que el interceptor consumirá
     * para autorizar o rechazar la solicitud.
     *
     * @param bucket        identificador del contador.
     * @param clientKey     clave del cliente (por ejemplo {@code u:42}
     *                      o {@code ip:10.0.0.1}).
     * @param limit         umbral máximo de solicitudes permitidas.
     * @param windowSeconds duración de la ventana temporal.
     * @return decisión con {@code allowed}, la cantidad restante y los
     *         segundos que faltan para que la ventana se reinicie.
     */
    public Decision check(String bucket, String clientKey, int limit, int windowSeconds) {
        long now = Instant.now().getEpochSecond();
        long windowIndex = now / windowSeconds;
        String key = "rl:%s:%s:%d".formatted(bucket, clientKey, windowIndex);
        long resetInSeconds = windowSeconds - (now % windowSeconds);
        try {
            Long count = redisTemplate.opsForValue().increment(key);
            long safeCount = count == null ? 1L : count;
            if (safeCount == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            boolean allowed = safeCount <= limit;
            long remaining = Math.max(0L, limit - safeCount);
            return new Decision(allowed, remaining, resetInSeconds);
        } catch (DataAccessException e) {
            // Si Redis falla, fallamos abierto: preferimos servir la
            // solicitud a bloquear al usuario por una infraestructura
            // caída. Se registra WARN para que sea visible en monitoreo.
            log.warn("Rate limiter no pudo comunicarse con Redis: {}", e.getMessage());
            return new Decision(true, limit, resetInSeconds);
        }
    }

    /**
     * Resultado de una consulta al rate limiter.
     *
     * @param allowed          {@code true} si la solicitud puede
     *                         proceder.
     * @param remaining        solicitudes restantes en la ventana
     *                         actual antes de rechazar la próxima.
     * @param resetInSeconds   segundos que faltan hasta el reinicio
     *                         de la ventana.
     */
    public record Decision(boolean allowed, long remaining, long resetInSeconds) {
    }
}
