package tech.impulso.labs.service;

import org.junit.jupiter.api.Test;
import tech.impulso.labs.config.CodeExecutionProperties;
import tech.impulso.labs.config.CodeExecutionProperties.LanguageProfile;
import tech.impulso.labs.service.CodeExecutionService.ExecutionRequest;
import tech.impulso.labs.service.CodeExecutionService.ExecutionResult;
import tech.impulso.labs.service.ProcessLauncher.ProcessOutcome;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del {@link DockerCliCodeExecutionService}.
 *
 * <p>Se ejercita con un {@link ProcessLauncher} en memoria para no
 * depender de la instalación real de Docker en el entorno de test. Se
 * verifica: la construcción segura de la línea de comando, el manejo
 * del código de salida y el timeout, y el rechazo de lenguajes no
 * configurados.</p>
 */
class DockerCliCodeExecutionServiceTest {

    @Test
    void rechazaLenguajeNoConfigurado() {
        CodeExecutionProperties props = baseProperties();
        RecordingLauncher launcher = new RecordingLauncher(
                new ProcessOutcome("", "", 0, false, 0));
        DockerCliCodeExecutionService service = new DockerCliCodeExecutionService(props, launcher);

        ExecutionResult result = service.execute(new ExecutionRequest("ruby", "puts 1", null, 1000));

        assertFalse(result.success());
        assertNull(result.exitCode());
        assertTrue(result.message().contains("no soportado"));
        assertNull(launcher.lastCommand, "no debe llamar a docker");
    }

    @Test
    void construyeComandoDockerConFlagsDeAislamiento() {
        CodeExecutionProperties props = baseProperties();
        RecordingLauncher launcher = new RecordingLauncher(
                new ProcessOutcome("hola\n", "", 0, false, 12));
        DockerCliCodeExecutionService service = new DockerCliCodeExecutionService(props, launcher);

        ExecutionResult result = service.execute(new ExecutionRequest("python", "print('hola')", null, 3000));

        assertTrue(result.success());
        assertEquals(0, result.exitCode());
        assertEquals("hola\n", result.stdout());
        List<String> cmd = launcher.lastCommand;
        assertNotNull(cmd);
        assertEquals("docker", cmd.get(0));
        assertEquals("run", cmd.get(1));
        assertTrue(cmd.contains("--rm"), "el contenedor debe eliminarse al terminar");
        assertTrue(cmd.contains("--network=none"), "no debe tener red");
        assertTrue(cmd.contains("--read-only"), "filesystem de solo lectura");
        assertTrue(cmd.contains("--cap-drop=ALL"), "sin capabilities");
        assertTrue(cmd.contains("--security-opt=no-new-privileges"));
        assertTrue(cmd.stream().anyMatch(s -> s.equals("--memory=128m")));
        assertTrue(cmd.stream().anyMatch(s -> s.equals("--cpus=0.5")));
        assertTrue(cmd.stream().anyMatch(s -> s.equals("--pids-limit=64")));
        assertTrue(cmd.stream().anyMatch(s -> s.equals("--user=65534:65534")));
        assertTrue(cmd.stream().anyMatch(s -> s.startsWith("--tmpfs=/tmp:")));
        assertTrue(cmd.contains("python:3.12-alpine"), "usa la imagen del perfil");
        assertTrue(cmd.contains("/workspace/main.py"),
                "el placeholder {file} se reemplaza por la ruta dentro del contenedor");
    }

    @Test
    void escribeCodigoEnDirectorioTemporalYLoLimpia() {
        CodeExecutionProperties props = baseProperties();
        // El launcher captura la ruta del workspace y verifica que el archivo exista al momento de ejecutar.
        RecordingLauncher launcher = new RecordingLauncher(
                new ProcessOutcome("", "", 0, false, 1)) {
            @Override
            public ProcessOutcome run(List<String> command, Path workingDir, String stdin,
                                     long timeoutMs, int maxOutputBytes) {
                this.workspaceAtRun = workingDir;
                this.codeFileExistsAtRun = Files.exists(workingDir.resolve("main.py"));
                return super.run(command, workingDir, stdin, timeoutMs, maxOutputBytes);
            }
        };
        DockerCliCodeExecutionService service = new DockerCliCodeExecutionService(props, launcher);

        service.execute(new ExecutionRequest("python", "print(2 + 2)", null, 1000));

        assertNotNull(launcher.workspaceAtRun);
        assertTrue(launcher.codeFileExistsAtRun, "el archivo de código debe existir durante la ejecución");
        assertFalse(Files.exists(launcher.workspaceAtRun), "el directorio temporal se elimina al terminar");
    }

    @Test
    void reportaTimeoutCuandoElProcesoNoTermina() {
        CodeExecutionProperties props = baseProperties();
        RecordingLauncher launcher = new RecordingLauncher(
                new ProcessOutcome("parcial", "", null, true, 5100));
        DockerCliCodeExecutionService service = new DockerCliCodeExecutionService(props, launcher);

        ExecutionResult result = service.execute(new ExecutionRequest("python", "while True: pass", null, 5000));

        assertFalse(result.success());
        assertNull(result.exitCode());
        assertTrue(result.message().contains("tiempo límite"));
        assertEquals("parcial", result.stdout());
    }

    @Test
    void limitaElTimeoutAlMaximoConfigurado() {
        CodeExecutionProperties props = baseProperties();
        props.setMaxTimeoutMs(2000);
        RecordingLauncher launcher = new RecordingLauncher(
                new ProcessOutcome("", "", 0, false, 100));
        DockerCliCodeExecutionService service = new DockerCliCodeExecutionService(props, launcher);

        service.execute(new ExecutionRequest("python", "print(1)", null, 60_000));

        // La espera del proceso Docker añade 2s de gracia sobre el timeout efectivo.
        assertEquals(4_000L, launcher.lastTimeoutMs, "el timeout se recorta al máximo configurado");
    }

    @Test
    void reportaCodigoDeSalidaDistintoDeCeroComoFallo() {
        CodeExecutionProperties props = baseProperties();
        RecordingLauncher launcher = new RecordingLauncher(
                new ProcessOutcome("", "boom", 2, false, 30));
        DockerCliCodeExecutionService service = new DockerCliCodeExecutionService(props, launcher);

        ExecutionResult result = service.execute(new ExecutionRequest("python", "raise Exception()", null, 1000));

        assertFalse(result.success());
        assertEquals(2, result.exitCode());
        assertEquals("boom", result.stderr());
        assertNotNull(result.message());
        assertTrue(result.message().contains("código 2"));
    }

    @Test
    void devuelveIndisponibleCuandoElLauncherFalla() {
        CodeExecutionProperties props = baseProperties();
        ProcessLauncher failing = (command, workingDir, stdin, timeoutMs, maxOutputBytes) -> {
            throw new IOException("docker no está disponible");
        };
        DockerCliCodeExecutionService service = new DockerCliCodeExecutionService(props, failing);

        ExecutionResult result = service.execute(new ExecutionRequest("python", "print(1)", null, 1000));

        assertFalse(result.success());
        assertNull(result.exitCode());
        assertTrue(result.message().toLowerCase().contains("sandbox"));
    }

    private static CodeExecutionProperties baseProperties() {
        CodeExecutionProperties props = new CodeExecutionProperties();
        LanguageProfile python = new LanguageProfile();
        python.setImage("python:3.12-alpine");
        python.setFilename("main.py");
        python.setCommand(List.of("python", "{file}"));
        props.setLanguages(Map.of("python", python));
        return props;
    }

    /**
     * Launcher en memoria que registra la última invocación y devuelve
     * un {@link ProcessOutcome} preprogramado.
     */
    private static class RecordingLauncher implements ProcessLauncher {
        private final ProcessOutcome outcome;
        List<String> lastCommand;
        long lastTimeoutMs;
        Path workspaceAtRun;
        boolean codeFileExistsAtRun;

        RecordingLauncher(ProcessOutcome outcome) {
            this.outcome = outcome;
        }

        @Override
        public ProcessOutcome run(List<String> command, Path workingDir, String stdin,
                                 long timeoutMs, int maxOutputBytes) {
            this.lastCommand = new ArrayList<>(command);
            this.lastTimeoutMs = timeoutMs;
            return outcome;
        }
    }
}
