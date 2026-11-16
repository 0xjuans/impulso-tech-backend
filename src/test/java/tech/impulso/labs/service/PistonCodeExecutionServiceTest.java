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
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.POST;

/**
 * Pruebas unitarias del {@link PistonCodeExecutionService}.
 *
 * <p>Usan {@link MockRestServiceServer} para interceptar las peticiones
 * HTTP salientes del {@link RestClient} sin necesidad de abrir un
 * puerto de red, evitando bloqueos de tráfico y dependencias externas.</p>
 */
class PistonCodeExecutionServiceTest {

    private static final String BASE_URL = "https://piston.test/api/v2/piston";

    private MockRestServiceServer mockServer;
    private PistonCodeExecutionService service;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();

        CodeExecutionProperties properties = new CodeExecutionProperties();
        properties.getPiston().setBaseUrl(BASE_URL);
        service = new PistonCodeExecutionService(properties, client);
    }

    @Test
    void ejecucionExitosaMapeaStdoutYCodigoDeSalida() {
        mockServer.expect(requestTo(BASE_URL + "/execute"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {
                          "language": "python",
                          "version": "3.10.0",
                          "run": {
                            "stdout": "hola\\n",
                            "stderr": "",
                            "code": 0,
                            "signal": null,
                            "output": "hola\\n"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        ExecutionResult result = service.execute(new ExecutionRequest("python", "print('hola')", "", 3000));

        assertTrue(result.success());
        assertEquals("hola\n", result.stdout());
        assertEquals(Integer.valueOf(0), result.exitCode());
        mockServer.verify();
    }

    @Test
    void errorDeCompilacionPriorizaSalidaDelCompilador() {
        mockServer.expect(requestTo(BASE_URL + "/execute"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {
                          "language": "java",
                          "version": "17.0.0",
                          "compile": {
                            "stdout": "",
                            "stderr": "error: expected ';'",
                            "code": 1,
                            "signal": null,
                            "output": ""
                          },
                          "run": {
                            "stdout": "",
                            "stderr": "",
                            "code": 0,
                            "signal": null,
                            "output": ""
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        ExecutionResult result = service.execute(new ExecutionRequest("java", "clase mala", "", 3000));

        assertFalse(result.success());
        assertEquals(Integer.valueOf(1), result.exitCode());
        assertTrue(result.stderr().contains("expected"));
        assertEquals("Error de compilación.", result.message());
    }

    @Test
    void timeoutSeReportaComoNoExitoso() {
        mockServer.expect(requestTo(BASE_URL + "/execute"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {
                          "language": "python",
                          "version": "3.10.0",
                          "run": {
                            "stdout": "",
                            "stderr": "",
                            "code": null,
                            "signal": "timeout",
                            "output": ""
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        ExecutionResult result = service.execute(new ExecutionRequest("python", "while True: pass", "", 1000));

        assertFalse(result.success());
        assertNotNull(result.message());
        assertTrue(result.message().contains("tiempo límite"));
    }

    @Test
    void lenguajeNoSoportadoDevuelveMensajeClaro() {
        ExecutionResult result = service.execute(new ExecutionRequest("brainfuck", "++.", "", 3000));

        assertFalse(result.success());
        assertTrue(result.message().contains("Lenguaje no soportado"));
    }
}
