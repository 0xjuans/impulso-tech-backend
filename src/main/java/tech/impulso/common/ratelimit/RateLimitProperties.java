package tech.impulso.common.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración externa del subsistema de rate limiting.
 *
 * <p>El namespace es {@code impulso.ratelimit.*}. Cuando el rate
 * limiting está desactivado los interceptores dejan pasar todas las
 * solicitudes; se usa exclusivamente para depurar o para entornos donde
 * Redis todavía no esté disponible.</p>
 */
@ConfigurationProperties(prefix = "impulso.ratelimit")
public class RateLimitProperties {

    /** Activa o desactiva el bloqueo por límite en toda la aplicación. */
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
