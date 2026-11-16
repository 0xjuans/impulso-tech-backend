package tech.impulso.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del proveedor de IA de la mascota (RF-017, RF-027).
 *
 * <p>El namespace es {@code impulso.ai.*}. Cuando el proveedor es
 * {@code stub} (predeterminado), la mascota responde con un mensaje
 * canónico local y no realiza llamadas de red. Cuando es
 * {@code anthropic} se usa la API de Claude y son obligatorias las
 * credenciales asociadas.</p>
 */
@ConfigurationProperties(prefix = "impulso.ai")
public class AiProperties {

    /** Proveedores soportados. */
    public enum Provider {
        /** Respuestas locales para desarrollo o pruebas. */
        STUB,
        /** API de Anthropic (Claude). */
        ANTHROPIC
    }

    private Provider provider = Provider.STUB;
    private final Anthropic anthropic = new Anthropic();

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }

    public Anthropic getAnthropic() {
        return anthropic;
    }

    /**
     * Configuración específica del proveedor Anthropic.
     */
    public static class Anthropic {
        /** Clave privada emitida por Anthropic; nunca debe salir del backend. */
        private String apiKey;
        /** Identificador del modelo a utilizar. */
        private String model = "claude-haiku-4-5-20251001";
        /** Tope superior de tokens generados por respuesta. */
        private int maxTokens = 400;
        /** Base URL del endpoint; permite apuntar a un proxy en tests. */
        private String baseUrl = "https://api.anthropic.com";
        /** Versión del contrato de la API. */
        private String version = "2023-06-01";
        /** Timeout de red aplicado a cada llamada, en milisegundos. */
        private int timeoutMs = 15_000;

        public String getApiKey() {
            return apiKey;
        }

        public void setApiKey(String apiKey) {
            this.apiKey = apiKey;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public int getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(int maxTokens) {
            this.maxTokens = maxTokens;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public void setBaseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
    }
}
