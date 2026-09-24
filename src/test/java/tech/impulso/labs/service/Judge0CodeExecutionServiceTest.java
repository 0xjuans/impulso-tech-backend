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
 * Pruebas unitarias del {@link Judge0CodeExecutionService}.
 *
 * <p>Validan el mapeo entre las respuestas de Judge0 CE y el
 * {@link ExecutionResult} del sistema: éxito, error de compilación,
 * timeout, lenguaje no soportado y ausencia de clave.</p>
 */
class Judge0CodeExecutionServiceTest {

    private static final String BASE_URL = "https://judge0.test";

    private MockRestServiceServer mockServer;
    private Judge0CodeExecutionService service;
    private CodeExecutionProperties properties;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();

        properties = new CodeExecutionProperties();
        properties.getJudge0().setBaseUrl(BASE_URL);
        properties.getJudge0().setApiKey("test-key");
        service = new Judge0CodeExecutionService(properties, client);
    }

    @Test
    void ejecucionExitosaEntregaStdoutYExit0() {
        mockServer.expect(requestTo(BASE_URL + "/submissions?base64_encoded=false&wait=true"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {
                          "stdout": "hola\\n",
                          "stderr": null,
                          "compile_output": null,
                          "message": null,
                          "exit_code": 0,
                          "time": "0.023",
                          "memory": 512,
                          "token": "abc",
                          "status": { "id": 3, "description": "Accepted" }
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
        mockServer.expect(requestTo(BASE_URL + "/submissions?base64_encoded=false&wait=true"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {
                          "stdout": null,
                          "stderr": null,
                          "compile_output": "error: expected ';'",
                          "message": null,
                          "exit_code": null,
                          "time": "0",
                          "memory": 0,
                          "token": "xyz",
                          "status": { "id": 6, "description": "Compilation Error" }
                        }
                        """, MediaType.APPLICATION_JSON));

        ExecutionResult result = service.execute(new ExecutionRequest("java", "clase mala", "", 3000));

        assertFalse(result.success());
        assertTrue(result.stderr().contains("expected"));
        assertEquals("Error de compilación.", result.message());
    }

    @Test
    void timeoutSeDetectaPorStatus5() {
        mockServer.expect(requestTo(BASE_URL + "/submissions?base64_encoded=false&wait=true"))
                .andExpect(method(POST))
                .andRespond(withSuccess("""
                        {
                          "stdout": null,
                          "stderr": null,
                          "compile_output": null,
                          "message": null,
                          "exit_code": null,
                          "time": "5.0",
                          "memory": 0,
                          "token": "xyz",
                          "status": { "id": 5, "description": "Time Limit Exceeded" }
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

    @Test
    void faltaDeApiKeyRechazaLaEjecucionConMensajeClaro() {
        properties.getJudge0().setApiKey("");

        ExecutionResult result = service.execute(new ExecutionRequest("python", "print(1)", "", 3000));

        assertFalse(result.success());
        assertTrue(result.message().contains("no está configurado"));
    }
}
