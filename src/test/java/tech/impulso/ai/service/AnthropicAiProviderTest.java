package tech.impulso.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import tech.impulso.ai.config.AiProperties;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pruebas del {@link AnthropicAiProvider} sin dependencia de la API
 * real: se apoya en {@link MockRestServiceServer} para interceptar las
 * peticiones y devolver respuestas controladas.
 *
 * <p>Cubre: mapping correcto del contrato (system + messages + headers
 * de autenticación), extracción del texto y tokens, y comportamiento
 * defensivo cuando la API falla.</p>
 */
class AnthropicAiProviderTest {

    private static final String API_KEY = "sk-test-123";
    private static final String BASE_URL = "https://mock.anthropic.local";

    private final ObjectMapper mapper = new ObjectMapper();
    private RestClient client;
    private MockRestServiceServer server;
    private AnthropicAiProvider provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        server = MockRestServiceServer.bindTo(builder).build();
        client = builder.build();
        provider = new AnthropicAiProvider(properties(), client);
    }

    @Test
    void faltaDeApiKeyImpideConstruirElProvider() {
        AiProperties props = new AiProperties();
        props.setProvider(AiProperties.Provider.ANTHROPIC);
        // sin api-key
        RestClient basic = RestClient.builder().baseUrl(BASE_URL).build();
        assertThrows(IllegalStateException.class,
                () -> new AnthropicAiProvider(props, basic));
    }

    @Test
    void completeConstruyePayloadYExtraeTextoYTokens() throws Exception {
        String responseJson = mapper.writeValueAsString(Map.of(
                "content", List.of(Map.of("type", "text", "text", "Hola, ¿en qué te ayudo?")),
                "usage", Map.of("input_tokens", 12, "output_tokens", 8)
        ));

        server.expect(requestTo(BASE_URL + "/v1/messages"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("x-api-key", API_KEY))
                .andExpect(header("anthropic-version", "2023-06-01"))
                .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.model").value("claude-haiku-4-5-20251001"))
                .andExpect(jsonPath("$.max_tokens").value(400))
                .andExpect(jsonPath("$.system").value("Eres la mascota"))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[0].content").value("Hola"))
                .andExpect(jsonPath("$.messages[1].role").value("assistant"))
                .andExpect(jsonPath("$.messages[1].content").value("Hola, ¿cómo estás?"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AiProvider.AiCompletion result = provider.complete(
                "Eres la mascota",
                List.of(
                        new AiProvider.AiTurn("user", "Hola"),
                        new AiProvider.AiTurn("assistant", "Hola, ¿cómo estás?")
                )
        );

        assertEquals("Hola, ¿en qué te ayudo?", result.content());
        assertNotNull(result.tokensUsed());
        assertEquals(20, result.tokensUsed());
        server.verify();
    }

    @Test
    void manejaFalloDeApiDevolviendoMensajeAmigable() {
        server.expect(requestTo(BASE_URL + "/v1/messages"))
                .andRespond(withServerError());

        AiProvider.AiCompletion result = provider.complete(
                "sys",
                List.of(new AiProvider.AiTurn("user", "hola"))
        );

        assertTrue(result.content().toLowerCase().contains("no pude"));
        assertNull(result.tokensUsed());
        server.verify();
    }

    @Test
    void manejaRespuestaSinContenidoConMensajeAmigable() throws Exception {
        String empty = mapper.writeValueAsString(Map.of("content", List.of(), "usage", Map.of()));
        server.expect(requestTo(BASE_URL + "/v1/messages"))
                .andRespond(withSuccess(empty, MediaType.APPLICATION_JSON));

        AiProvider.AiCompletion result = provider.complete(
                "sys",
                List.of(new AiProvider.AiTurn("user", "hola"))
        );

        assertTrue(result.content().toLowerCase().contains("palabras"));
        server.verify();
    }

    @Test
    void concatenaMultiplesBloquesDeTexto() throws Exception {
        String responseJson = mapper.writeValueAsString(Map.of(
                "content", List.of(
                        Map.of("type", "text", "text", "Parte 1. "),
                        Map.of("type", "text", "text", "Parte 2."),
                        Map.of("type", "tool_use", "text", "ignorado")
                )
        ));
        server.expect(requestTo(BASE_URL + "/v1/messages"))
                .andRespond(withSuccess(responseJson, MediaType.APPLICATION_JSON));

        AiProvider.AiCompletion result = provider.complete("sys",
                List.of(new AiProvider.AiTurn("user", "x")));

        assertEquals("Parte 1. Parte 2.", result.content());
    }

    private static AiProperties properties() {
        AiProperties props = new AiProperties();
        props.setProvider(AiProperties.Provider.ANTHROPIC);
        props.getAnthropic().setApiKey(API_KEY);
        props.getAnthropic().setBaseUrl(BASE_URL);
        return props;
    }
}
