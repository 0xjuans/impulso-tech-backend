package tech.impulso.ai.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tech.impulso.ai.config.AiProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementación de {@link AiProvider} contra la API de Anthropic
 * (Claude Messages API).
 *
 * <p>Sólo se instancia cuando {@code impulso.ai.provider=anthropic}; en
 * ausencia de credenciales válidas, el {@link StubAiProvider} sigue
 * siendo el bean activo por su {@code @ConditionalOnMissingBean}.</p>
 *
 * <p>El prompt del sistema se envía en el campo {@code system} de la
 * API, separado del historial de mensajes. Esto refuerza la separación
 * requerida por §29 del {@code CLAUDE.md}: las instrucciones nunca se
 * concatenan con contenido controlado por el usuario.</p>
 */
@Component
@ConditionalOnProperty(prefix = "impulso.ai", name = "provider", havingValue = "anthropic")
public class AnthropicAiProvider implements AiProvider {

    private static final Logger log = LoggerFactory.getLogger(AnthropicAiProvider.class);

    private final AiProperties.Anthropic config;
    private final RestClient client;

    @Autowired
    public AnthropicAiProvider(AiProperties properties) {
        this(properties, defaultClient(properties.getAnthropic()));
    }

    /**
     * Constructor visible en pruebas para inyectar un {@link RestClient}
     * apuntado a un servidor mock.
     */
    AnthropicAiProvider(AiProperties properties, RestClient client) {
        this.config = properties.getAnthropic();
        this.client = client;
        if (this.config.getApiKey() == null || this.config.getApiKey().isBlank()) {
            throw new IllegalStateException(
                    "impulso.ai.provider=anthropic requiere configurar impulso.ai.anthropic.api-key");
        }
    }

    @Override
    public AiCompletion complete(String systemPrompt, List<AiTurn> history) {
        List<Map<String, Object>> messages = new ArrayList<>(history.size());
        for (AiTurn turn : history) {
            messages.add(Map.of(
                    "role", "user".equalsIgnoreCase(turn.role()) ? "user" : "assistant",
                    "content", turn.content() == null ? "" : turn.content()
            ));
        }
        Map<String, Object> body = Map.of(
                "model", config.getModel(),
                "max_tokens", config.getMaxTokens(),
                "system", systemPrompt,
                "messages", messages
        );

        try {
            AnthropicResponse response = client.post()
                    .uri("/v1/messages")
                    .header("x-api-key", config.getApiKey())
                    .header("anthropic-version", config.getVersion())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(AnthropicResponse.class);
            return toCompletion(response);
        } catch (RestClientException e) {
            log.warn("Fallo llamando a Anthropic Messages API: {}", e.getMessage());
            return new AiCompletion(
                    "Lo siento, no pude generar una respuesta en este momento. "
                            + "Intenta nuevamente en unos segundos.",
                    null
            );
        }
    }

    private static AiCompletion toCompletion(AnthropicResponse response) {
        if (response == null || response.content == null || response.content.isEmpty()) {
            return new AiCompletion(
                    "La mascota se quedó sin palabras. Intenta reformular tu pregunta.",
                    null
            );
        }
        StringBuilder text = new StringBuilder();
        for (AnthropicResponse.Block block : response.content) {
            if ("text".equalsIgnoreCase(block.type) && block.text != null) {
                text.append(block.text);
            }
        }
        Integer tokens = null;
        if (response.usage != null) {
            int total = 0;
            if (response.usage.input_tokens != null) total += response.usage.input_tokens;
            if (response.usage.output_tokens != null) total += response.usage.output_tokens;
            if (total > 0) tokens = total;
        }
        return new AiCompletion(text.toString().strip(), tokens);
    }

    /**
     * Construye el {@link RestClient} configurando el base URL y los
     * timeouts definidos en las propiedades.
     */
    private static RestClient defaultClient(AiProperties.Anthropic config) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(config.getTimeoutMs()));
        factory.setReadTimeout(Duration.ofMillis(config.getTimeoutMs()));
        return RestClient.builder()
                .baseUrl(config.getBaseUrl())
                .requestFactory(factory)
                .build();
    }

    /**
     * Representación mínima de la respuesta de la Messages API,
     * suficiente para extraer el texto y el uso de tokens. Los campos
     * adicionales se ignoran para tolerar cambios menores del contrato.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class AnthropicResponse {
        public List<Block> content;
        public Usage usage;

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Block {
            public String type;
            public String text;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        static class Usage {
            public Integer input_tokens;
            public Integer output_tokens;
        }
    }
}
