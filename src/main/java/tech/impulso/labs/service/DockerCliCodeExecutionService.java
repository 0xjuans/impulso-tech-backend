package tech.impulso.labs.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tech.impulso.labs.config.CodeExecutionProperties;
import tech.impulso.labs.config.CodeExecutionProperties.LanguageProfile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Implementación de {@link CodeExecutionService} que delega la ejecución
 * de código en un contenedor Docker efímero (RF-030, RF-037).
 *
 * <p>Cada solicitud escribe el código del estudiante en un directorio
 * temporal del host, monta ese directorio como {@code /workspace} de
 * solo lectura dentro del contenedor y ejecuta la línea de comando
 * definida por el perfil del lenguaje. El contenedor se lanza con la
 * red deshabilitada, sistema de archivos raíz de solo lectura, cambio
 * de usuario a uno sin privilegios, límites de CPU, memoria, procesos y
 * un timeout aplicado a la espera del proceso Docker. Al finalizar se
 * elimina el directorio temporal.</p>
 *
 * <p>Sólo se activa cuando la propiedad
 * {@code impulso.code-exec.mode=docker-cli} está presente; en otros
 * modos el {@link StubCodeExecutionService} sigue siendo el bean
 * registrado por defecto.</p>
 */
@Component
@ConditionalOnProperty(prefix = "impulso.code-exec", name = "mode", havingValue = "docker-cli")
public class DockerCliCodeExecutionService implements CodeExecutionService {

    private static final Logger log = LoggerFactory.getLogger(DockerCliCodeExecutionService.class);

    private final CodeExecutionProperties properties;
    private final ProcessLauncher launcher;

    @Autowired
    public DockerCliCodeExecutionService(CodeExecutionProperties properties) {
        this(properties, new DefaultProcessLauncher());
    }

    /**
     * Constructor destinado a las pruebas que permite inyectar un
     * {@link ProcessLauncher} controlado sin dependencia de Docker.
     */
    DockerCliCodeExecutionService(CodeExecutionProperties properties, ProcessLauncher launcher) {
        this.properties = properties;
        this.launcher = launcher;
    }

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        String language = normalizeLanguage(request.language());
        LanguageProfile profile = properties.getLanguages().get(language);
        if (profile == null || profile.getImage() == null || profile.getFilename() == null) {
            return unavailable("Lenguaje no soportado por el sandbox: " + language);
        }
        int timeoutMs = properties.resolveTimeoutMs(request.timeoutMs());

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("impulso-lab-");
            Path codeFile = tempDir.resolve(profile.getFilename());
            String code = request.code() == null ? "" : request.code();
            Files.writeString(codeFile, code, StandardCharsets.UTF_8);
            restrictPermissions(codeFile);

            List<String> command = buildDockerCommand(profile, tempDir);
            ProcessLauncher.ProcessOutcome outcome = launcher.run(
                    command,
                    tempDir,
                    request.stdin(),
                    timeoutMs + 2_000L,
                    properties.getMaxOutputBytes()
            );

            if (outcome.timedOut()) {
                return new ExecutionResult(
                        outcome.stdout(),
                        outcome.stderr(),
                        outcome.exitCode(),
                        outcome.durationMs(),
                        false,
                        "La ejecución superó el tiempo límite de " + timeoutMs + " ms y fue detenida."
                );
            }

            Integer exit = outcome.exitCode();
            boolean success = exit != null && exit == 0;
            String message = success ? null : "El proceso terminó con código " + exit;
            return new ExecutionResult(
                    outcome.stdout(),
                    outcome.stderr(),
                    exit,
                    outcome.durationMs(),
                    success,
                    message
            );
        } catch (IOException e) {
            log.warn("Fallo al preparar la ejecución de código: {}", e.getMessage());
            return unavailable("No fue posible preparar la ejecución de código en el sandbox.");
        } finally {
            if (tempDir != null) {
                deleteRecursively(tempDir);
            }
        }
    }

    /**
     * Construye la línea de comando {@code docker run} con los flags de
     * aislamiento requeridos por §30 del CLAUDE.md.
     */
    List<String> buildDockerCommand(LanguageProfile profile, Path hostWorkspace) {
        List<String> cmd = new ArrayList<>();
        cmd.add(properties.getDockerBinary());
        cmd.add("run");
        cmd.add("--rm");
        cmd.add("-i");
        cmd.add("--network=none");
        cmd.add("--read-only");
        cmd.add("--memory=" + properties.getMemoryLimit());
        cmd.add("--memory-swap=" + properties.getMemoryLimit());
        cmd.add("--cpus=" + properties.getCpus());
        cmd.add("--pids-limit=" + properties.getPidsLimit());
        cmd.add("--cap-drop=ALL");
        cmd.add("--security-opt=no-new-privileges");
        cmd.add("--user=" + properties.getUser());
        cmd.add("--tmpfs=/tmp:rw,noexec,nosuid,size=" + properties.getTmpfsSize());
        cmd.add("--workdir=/workspace");
        cmd.add("--stop-timeout=1");
        cmd.add("-v");
        cmd.add(hostWorkspace.toAbsolutePath().toString() + ":/workspace:ro");
        cmd.add(profile.getImage());
        String containerFile = "/workspace/" + profile.getFilename();
        for (String arg : profile.getCommand()) {
            cmd.add(arg == null ? "" : arg.replace("{file}", containerFile));
        }
        return cmd;
    }

    private static String normalizeLanguage(String language) {
        return language == null ? "" : language.trim().toLowerCase();
    }

    private static ExecutionResult unavailable(String message) {
        return new ExecutionResult("", "", null, 0, false, message);
    }

    /**
     * Restringe los permisos del archivo temporal cuando el sistema de
     * ficheros lo soporta. En Windows la operación se ignora en silencio.
     */
    private static void restrictPermissions(Path file) {
        try {
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rw-r--r--"));
        } catch (UnsupportedOperationException | IOException ignored) {
            // No POSIX (Windows) o error transitorio: los permisos por defecto son suficientes.
        }
    }

    private static void deleteRecursively(Path root) {
        try (Stream<Path> paths = Files.walk(root)) {
            paths.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException ignored) {
                    // Se descarta: la limpieza es de mejor esfuerzo.
                }
            });
        } catch (IOException ignored) {
            // Se descarta: la limpieza es de mejor esfuerzo.
        }
    }
}
