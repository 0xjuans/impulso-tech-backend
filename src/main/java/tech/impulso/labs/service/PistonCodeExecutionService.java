package tech.impulso.labs.service;

import java.time.Duration;
import java.util.List;
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
 * de código en el sandbox público de Piston (RF-030, RF-037).
 *
 * <p>Piston (<a href="https://github.com/engineer-man/piston">engineer-man/piston</a>)
 * ejecuta cada solicitud en un contenedor aislado con red deshabilitada,
 * usuario sin privilegios y límites estrictos de CPU y memoria,
 * ofreciendo exactamente las garantías de la §30 del CLAUDE.md sin
 * requerir Docker en el propio host del backend.</p>
 *
 * <p>Se activa cuando la propiedad {@code impulso.code-exec.mode=piston}
 * está presente. Es una alternativa práctica al modo {@code docker-cli}
 * para despliegues donde el host no expone el demonio Docker (por
 * ejemplo, Fly.io).</p>
 */
@Component
@ConditionalOnProperty(prefix = "impulso.code-exec", name = "mode", havingValue = "piston")
public class PistonCodeExecutionService implements CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(PistonCodeExecutionService.class);

    /**
     * Mapa desde el identificador de lenguaje interno de Impulso al
     * nombre esperado por Piston. Los alias de Piston se documentan en
     * {@code /api/v2/piston/runtimes}. Se usa {@code "*"} como versión
     * para pedir el runtime más reciente disponible.
     */
    private static final Map<String, String> LANGUAGE_MAP = Map.of(
            "python", "python",
            "javascript", "javascript",
            "js", "javascript",
            "typescript", "typescript",
            "ts", "typescript",
            "java", "java",
            "c", "c",
            "cpp", "c++",
            "csharp", "csharp"
    );

    /** Nombre del archivo que se envía a Piston por lenguaje. */
    private static final Map<String, String> FILENAME_MAP = Map.of(
            "python", "main.py",
            "javascript", "main.js",
            "typescript", "main.ts",
            "java", "Main.java",
            "c", "main.c",
            "c++", "main.cpp",
            "csharp", "main.cs"
    );

    private final CodeExecutionProperties properties;
    private final RestClient client;

    @Autowired
    public PistonCodeExecutionService(CodeExecutionProperties properties) {
        this(properties, RestClient.builder()
                .baseUrl(properties.getPiston().getBaseUrl())
                .build());
    }

    /** Constructor visible en pruebas para inyectar un {@link RestClient} controlado. */
    PistonCodeExecutionService(CodeExecutionProperties properties, RestClient client) {
        this.properties = properties;
        this.client = client;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        String normalized = normalize(request.language());
        String pistonLanguage = LANGUAGE_MAP.get(normalized);
        if (pistonLanguage == null) {
            return fail("Lenguaje no soportado por el sandbox: " + normalized);
        }
        int timeoutMs = properties.resolveTimeoutMs(request.timeoutMs());
        String filename = FILENAME_MAP.getOrDefault(pistonLanguage, "main.txt");

        Map<String, Object> body = Map.of(
                "language", pistonLanguage,
                "version", "*",
                "files", List.of(Map.of(
                        "name", filename,
                        "content", request.code() == null ? "" : request.code())),
                "stdin", request.stdin() == null ? "" : request.stdin(),
                "run_timeout", timeoutMs,
                "compile_timeout", timeoutMs
        );

        long start = System.nanoTime();
        PistonResponse response;
        try {
            response = client.post()
                    .uri("/execute")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(PistonResponse.class);
        } catch (RestClientException e) {
            log.warn("Piston no respondió correctamente: {}", e.getMessage());
            return fail("El sandbox de ejecución no está disponible en este momento.");
        }
        int elapsed = (int) TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

        if (response == null) {
            return fail("Respuesta vacía del sandbox de ejecución.");
        }

        int maxOut = properties.getMaxOutputBytes();
        Stage run = response.run();
        Stage compile = response.compile();

        // Si la etapa de compilación falla, priorizamos su salida para
        // que el estudiante vea el error del compilador.
        if (compile != null && compile.code() != null && compile.code() != 0) {
            return new ExecutionResult(
                    truncate(compile.stdout(), maxOut),
                    truncate(compile.stderr(), maxOut),
                    compile.code(),
                    elapsed,
                    false,
                    "Error de compilación."
            );
        }

        if (run == null) {
            return fail("El sandbox no devolvió resultado de ejecución.");
        }

        boolean timedOut = "timeout".equalsIgnoreCase(run.signal());
        Integer exit = run.code();
        boolean success = !timedOut && exit != null && exit == 0;
        String message;
        if (timedOut) {
            message = "La ejecución superó el tiempo límite de " + timeoutMs + " ms y fue detenida.";
        } else if (!success) {
            message = "El proceso terminó con código " + exit + (run.signal() != null ? " (" + run.signal() + ")" : "");
        } else {
            message = null;
        }

        return new ExecutionResult(
                truncate(run.stdout(), maxOut),
                truncate(run.stderr(), maxOut),
                exit,
                elapsed,
                success,
                message
        );
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

    /** Timeout total tolerable, sumando compilación + ejecución + margen. */
    @SuppressWarnings("unused")
    private Duration totalTimeout() {
        return Duration.ofMillis(properties.getPiston().getReadTimeoutMs());
    }

    /** Fragmento de la respuesta correspondiente a una etapa (run/compile). */
    public record Stage(String stdout, String stderr, Integer code, String signal, String output) {
    }

    /** Cuerpo devuelto por Piston al ejecutar código. */
    public record PistonResponse(String language, String version, Stage run, Stage compile) {
    }
}
