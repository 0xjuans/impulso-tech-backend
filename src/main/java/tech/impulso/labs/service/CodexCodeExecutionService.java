package tech.impulso.labs.service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import tech.impulso.labs.config.CodeExecutionProperties;

/**
 * Implementación de {@link CodeExecutionService} que delega la ejecución
 * de código en la API pública de CodeX (RF-030, RF-037).
 *
 * <p>CodeX (<a href="https://github.com/Jaagrav/CodeX-API">Jaagrav/CodeX-API</a>)
 * expone un endpoint gratuito y sin autenticación en
 * {@code https://api.codex.jaagrav.in/} que compila y ejecuta código en
 * un contenedor aislado. Se usa como alternativa a Piston (cuya API
 * pública pasó a ser whitelist-only) y al modo Docker local, respetando
 * la §30 del CLAUDE.md: el código del estudiante nunca corre dentro del
 * proceso principal.</p>
 *
 * <p>Se activa cuando {@code impulso.code-exec.mode=codex}.</p>
 */
@Component
@ConditionalOnProperty(prefix = "impulso.code-exec", name = "mode", havingValue = "codex")
public class CodexCodeExecutionService implements CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(CodexCodeExecutionService.class);

    /**
     * Mapa del identificador interno de lenguaje al código que espera
     * CodeX. Los identificadores soportados están documentados en el
     * repositorio del servicio.
     */
    private static final Map<String, String> LANGUAGE_MAP = Map.ofEntries(
            Map.entry("python", "py"),
            Map.entry("py", "py"),
            Map.entry("javascript", "js"),
            Map.entry("js", "js"),
            Map.entry("java", "java"),
            Map.entry("c", "c"),
            Map.entry("cpp", "cpp"),
            Map.entry("c++", "cpp"),
            Map.entry("csharp", "cs"),
            Map.entry("cs", "cs"),
            Map.entry("go", "go")
    );

    private final CodeExecutionProperties properties;
    private final RestClient client;

    @Autowired
    public CodexCodeExecutionService(CodeExecutionProperties properties) {
        this(properties, RestClient.builder()
                .baseUrl(properties.getCodex().getBaseUrl())
                .build());
    }

    /** Constructor visible en pruebas para inyectar un cliente controlado. */
    CodexCodeExecutionService(CodeExecutionProperties properties, RestClient client) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        String normalized = normalize(request.language());
        String codexLanguage = LANGUAGE_MAP.get(normalized);
        if (codexLanguage == null) {
            return fail("Lenguaje no soportado por el sandbox: " + normalized);
        }
        int timeoutMs = properties.resolveTimeoutMs(request.timeoutMs());

        Map<String, Object> body = Map.of(
                "code", request.code() == null ? "" : request.code(),
                "language", codexLanguage,
                "input", request.stdin() == null ? "" : request.stdin()
        );

        long start = System.nanoTime();
        CodexResponse response;
        try {
            response = client.post()
                    .uri("/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(CodexResponse.class);
        } catch (RestClientException e) {
            log.warn("CodeX no respondió correctamente: {}", e.getMessage());
            return fail("El sandbox de ejecución no está disponible en este momento.");
        }
        int elapsed = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        if (response == null) {
            return fail("Respuesta vacía del sandbox de ejecución.");
        }

        int maxOut = properties.getMaxOutputBytes();
        String stdout = truncate(response.output(), maxOut);
        String stderr = truncate(response.error(), maxOut);
        boolean hasError = stderr != null && !stderr.isBlank();
        boolean statusOk = response.status() != null && response.status() == 200;
        boolean success = statusOk && !hasError;

        // CodeX aplica su propio timeout del lado del servidor. Si el
        // programa quedó colgado, suele reportarlo en `error`.
        boolean looksLikeTimeout = hasError && stderr.toLowerCase().contains("timeout");

        String message;
        if (!statusOk) {
            message = "El sandbox rechazó la ejecución.";
        } else if (looksLikeTimeout) {
            message = "La ejecución superó el tiempo límite de " + timeoutMs + " ms y fue detenida.";
        } else if (hasError) {
            message = "El proceso terminó con errores.";
        } else {
            message = null;
        }

        // CodeX no devuelve el exit code real; se infiere de la ausencia
        // o presencia de errores para mantener el contrato con el
        // resto del sistema.
        Integer exit = success ? 0 : (hasError ? 1 : null);

        return new ExecutionResult(stdout, stderr, exit, elapsed, success, message);
    }

    private static String normalize(String language) {
        return language == null ? "" : language.trim().toLowerCase();
    }

    private static ExecutionResult fail(String message) {
        return new ExecutionResult("", "", null, 0, false, message);
    }

    private static String truncate(String text, int maxBytes) {
        if (text == null) return "";
        byte[] bytes = text.getBytes();
        if (bytes.length <= maxBytes) return text;
        return new String(bytes, 0, maxBytes) + "\n… (salida recortada)";
    }

    /** Cuerpo devuelto por CodeX al ejecutar código. */
    public record CodexResponse(
            String timeStamp,
            Integer status,
            String output,
            String error,
            String language,
            String info) {
    }
}
