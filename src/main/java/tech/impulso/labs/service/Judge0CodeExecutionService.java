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
 * de código en la API pública de Judge0 CE (RF-030, RF-037).
 *
 * <p>Judge0 (<a href="https://judge0.com">judge0.com</a>) es un sandbox
 * estable y ampliamente utilizado para plataformas educativas. Se
 * consume a través de RapidAPI, que ofrece 50 ejecuciones diarias en
 * su plan gratuito. Es la opción de mayor confiabilidad tras el cierre
 * de las APIs públicas de Piston y CodeX.</p>
 *
 * <p>Se activa cuando {@code impulso.code-exec.mode=judge0}. La clave
 * de RapidAPI se lee del secret {@code CODE_EXEC_JUDGE0_KEY} y la URL
 * base se resuelve por defecto al endpoint oficial
 * {@code https://judge0-ce.p.rapidapi.com}.</p>
 */
@Component
@ConditionalOnProperty(prefix = "impulso.code-exec", name = "mode", havingValue = "judge0")
public class Judge0CodeExecutionService implements CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(Judge0CodeExecutionService.class);

    /**
     * Identificadores numéricos de lenguaje esperados por Judge0 CE
     * (ver {@code GET /languages}). Los valores están alineados con los
     * más recientes disponibles en la versión pública.
     */
    private static final Map<String, Integer> LANGUAGE_MAP = Map.ofEntries(
            Map.entry("python", 71),      // Python 3.8.1
            Map.entry("py", 71),
            Map.entry("javascript", 63),  // JavaScript (Node.js 12.14.0)
            Map.entry("js", 63),
            Map.entry("java", 62),        // Java (OpenJDK 13.0.1)
            Map.entry("c", 50),           // C (GCC 9.2.0)
            Map.entry("cpp", 54),         // C++ (GCC 9.2.0)
            Map.entry("c++", 54),
            Map.entry("csharp", 51),      // C# (Mono 6.6.0.161)
            Map.entry("cs", 51),
            Map.entry("go", 60),          // Go (1.13.5)
            Map.entry("rust", 73),        // Rust (1.40.0)
            Map.entry("ruby", 72)         // Ruby (2.7.0)
    );

    private final CodeExecutionProperties properties;
    private final RestClient client;

    @Autowired
    public Judge0CodeExecutionService(CodeExecutionProperties properties) {
        this(properties, RestClient.builder()
                .baseUrl(properties.getJudge0().getBaseUrl())
                .build());
    }

    /** Constructor visible en pruebas para inyectar un cliente controlado. */
    Judge0CodeExecutionService(CodeExecutionProperties properties, RestClient client) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        String normalized = normalize(request.language());
        Integer languageId = LANGUAGE_MAP.get(normalized);
        if (languageId == null) {
            return fail("Lenguaje no soportado por el sandbox: " + normalized);
        }
        String apiKey = properties.getJudge0().getApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Judge0 no está configurado: falta CODE_EXEC_JUDGE0_KEY.");
            return fail("El sandbox de ejecución no está configurado. Contacta al administrador.");
        }

        int timeoutMs = properties.resolveTimeoutMs(request.timeoutMs());
        Map<String, Object> body = Map.of(
                "source_code", request.code() == null ? "" : request.code(),
                "language_id", languageId,
                "stdin", request.stdin() == null ? "" : request.stdin(),
                "cpu_time_limit", Math.max(1, timeoutMs / 1000),
                "wall_time_limit", Math.max(2, (timeoutMs / 1000) + 2)
        );

        long start = System.nanoTime();
        Judge0Response response;
        try {
            response = client.post()
                    .uri("/submissions?base64_encoded=false&wait=true")
                    .header("X-RapidAPI-Key", apiKey)
                    .header("X-RapidAPI-Host", properties.getJudge0().getRapidApiHost())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(Judge0Response.class);
        } catch (RestClientException e) {
            log.warn("Judge0 no respondió correctamente: {}", e.getMessage());
            return fail("El sandbox de ejecución no está disponible en este momento.");
        }
        int elapsed = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        if (response == null) {
            return fail("Respuesta vacía del sandbox de ejecución.");
        }

        int maxOut = properties.getMaxOutputBytes();
        String stdout = truncate(response.stdout(), maxOut);
        String stderr = truncate(joinNonBlank(response.compile_output(), response.stderr()), maxOut);
        Integer exit = response.exit_code();
        Judge0Status status = response.status();
        int statusId = status == null || status.id() == null ? -1 : status.id();

        // Códigos de estado de Judge0:
        //  3  → Accepted (éxito)
        //  4  → Wrong Answer
        //  5  → Time Limit Exceeded
        //  6  → Compilation Error
        //  7-12 → Runtime errors
        //  13 → Internal Error
        //  14 → Exec Format Error
        boolean success = statusId == 3;
        boolean timedOut = statusId == 5;
        boolean compileError = statusId == 6;

        String message;
        if (success) {
            message = null;
        } else if (timedOut) {
            message = "La ejecución superó el tiempo límite de " + timeoutMs + " ms y fue detenida.";
        } else if (compileError) {
            message = "Error de compilación.";
        } else if (status != null && status.description() != null) {
            message = "El proceso terminó: " + status.description();
        } else {
            message = "El proceso terminó con errores.";
        }

        return new ExecutionResult(stdout, stderr, exit, elapsed, success, message);
    }

    private static String joinNonBlank(String a, String b) {
        boolean hasA = a != null && !a.isBlank();
        boolean hasB = b != null && !b.isBlank();
        if (hasA && hasB) return a + "\n" + b;
        if (hasA) return a;
        if (hasB) return b;
        return "";
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

    /** Descripción del estado de la ejecución devuelto por Judge0. */
    public record Judge0Status(Integer id, String description) {
    }

    /** Cuerpo devuelto por Judge0 al ejecutar una submisión con {@code wait=true}. */
    @SuppressWarnings("checkstyle:parametername")
    public record Judge0Response(
            String stdout,
            String stderr,
            String compile_output,
            String message,
            Integer exit_code,
            String time,
            Integer memory,
            String token,
            Judge0Status status) {
    }
}
