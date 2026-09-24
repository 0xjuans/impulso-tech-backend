package tech.impulso.labs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import tech.impulso.labs.config.CodeExecutionProperties;
import tech.impulso.labs.service.CodeExecutionService.ExecutionRequest;
import tech.impulso.labs.service.CodeExecutionService.ExecutionResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Pruebas unitarias del {@link CodexCodeExecutionService}.
 *
 * <p>Interceptan las peticiones al cliente HTTP con
 * {@link MockRestServiceServer} para validar el mapeo entre las
 * respuestas de CodeX y el {@link ExecutionResult} del sistema.</p>
 */
class CodexCodeExecutionServiceTest {

    private static final String BASE_URL = "https://codex.test";

    private MockRestServiceServer mockServer;
    private CodexCodeExecutionService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();

        CodeExecutionProperties properties = new CodeExecutionProperties();
        properties.getCodex().setBaseUrl(BASE_URL);
        service = new CodexCodeExecutionService(properties, client);
    }

    @Test
    void ejecucionExitosaEntregaStdoutYExit0() {
        mockServer.expect(requestTo(BASE_URL + "/"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {
                          "timeStamp": "2026-09-22T20:00:00Z",
                          "status": 200,
                          "output": "hola\\n",
                          "error": "",
                          "language": "py",
                          "info": ""
                        }
                        """, MediaType.APPLICATION_JSON));

        ExecutionResult result = service.execute(new ExecutionRequest("python", "print('hola')", "", 3000));

        assertTrue(result.success());
        assertEquals("hola\n", result.stdout());
        assertEquals(Integer.valueOf(0), result.exitCode());
        mockServer.verify();
    }

    @Test
    void errorEnEjecucionSePropagaEnStderr() {
        mockServer.expect(requestTo(BASE_URL + "/"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {
                          "timeStamp": "2026-09-22T20:00:00Z",
                          "status": 200,
                          "output": "",
                          "error": "NameError: name 'x' is not defined",
                          "language": "py",
                          "info": ""
                        }
                        """, MediaType.APPLICATION_JSON));

        ExecutionResult result = service.execute(new ExecutionRequest("python", "print(x)", "", 3000));

        assertFalse(result.success());
        assertTrue(result.stderr().contains("NameError"));
        assertEquals(Integer.valueOf(1), result.exitCode());
        assertNotNull(result.message());
    }

    @Test
    void timeoutSeDetectaPorTextoEnStderr() {
        mockServer.expect(requestTo(BASE_URL + "/"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {
                          "timeStamp": "2026-09-22T20:00:00Z",
                          "status": 200,
                          "output": "",
                          "error": "Execution Timeout",
                          "language": "py",
                          "info": ""
                        }
                        """, MediaType.APPLICATION_JSON));

        ExecutionResult result = service.execute(new ExecutionRequest("python", "while True: pass", "", 1000));

        assertFalse(result.success());
        assertTrue(result.message().contains("tiempo límite"));
    }

    @Test
    void lenguajeNoSoportadoDevuelveMensajeClaro() {
        ExecutionResult result = service.execute(new ExecutionRequest("brainfuck", "++.", "", 3000));

        assertFalse(result.success());
        assertTrue(result.message().contains("Lenguaje no soportado"));
    }
}
