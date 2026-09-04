package tech.impulso.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración externa de los orígenes permitidos para CORS.
 *
 * <p>Se lee desde la propiedad {@code app.cors.allowed-origins}, que
 * admite una lista separada por comas. En desarrollo suele apuntar al
 * frontend local; en producción debe restringirse al dominio real de
 * la aplicación.</p>
 *
 * @param allowedOrigins orígenes autorizados, separados por comas.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(String allowedOrigins) {

    /**
     * Devuelve los orígenes ya divididos y normalizados. Devuelve una
     * lista vacía cuando el valor no está definido.
     */
    public List<String> origins() {
        if (allowedOrigins == null || allowedOrigins.isBlank()) {
            return List.of();
        }
        return Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
