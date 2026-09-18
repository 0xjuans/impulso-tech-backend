package tech.impulso.labs.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Implementación por defecto de {@link ProcessLauncher} basada en
 * {@link ProcessBuilder}.
 *
 * <p>Se encarga de escribir el {@code stdin} suministrado, drenar los
 * flujos de salida en hebras dedicadas (para evitar bloqueos por buffers
 * llenos), aplicar el timeout y devolver la salida capturada
 * respetando el tope máximo de bytes.</p>
 */
class DefaultProcessLauncher implements ProcessLauncher {

    @Override
    public ProcessOutcome run(List<String> command,
                              Path workingDir,
                              String stdin,
                              long timeoutMs,
                              int maxOutputBytes) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDir != null) {
            builder.directory(workingDir.toFile());
        }
        long start = System.currentTimeMillis();
        Process process = builder.start();

        writeStdin(process, stdin);

        ExecutorService pool = Executors.newFixedThreadPool(2, r -> {
            Thread t = new Thread(r, "code-exec-reader");
            t.setDaemon(true);
            return t;
        });
        Future<String> stdoutFuture = pool.submit(() -> readCapped(process.getInputStream(), maxOutputBytes));
        Future<String> stderrFuture = pool.submit(() -> readCapped(process.getErrorStream(), maxOutputBytes));
        pool.shutdown();

        boolean finished;
        try {
            finished = process.waitFor(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            return new ProcessOutcome("", "", null, true, elapsed(start));
        }

        if (!finished) {
            process.destroyForcibly();
            try {
                process.waitFor(2, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            return new ProcessOutcome(
                    safeGet(stdoutFuture),
                    safeGet(stderrFuture),
                    null,
                    true,
                    elapsed(start)
            );
        }

        int duration = elapsed(start);
        return new ProcessOutcome(
                safeGet(stdoutFuture),
                safeGet(stderrFuture),
                process.exitValue(),
                false,
                duration
        );
    }

    /**
     * Escribe el stdin del proceso ignorando errores derivados de que el
     * hijo lo haya cerrado antes de recibirlo.
     */
    private static void writeStdin(Process process, String stdin) {
        try (OutputStream os = process.getOutputStream()) {
            if (stdin != null && !stdin.isEmpty()) {
                os.write(stdin.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
        } catch (IOException ignored) {
            // El proceso cerró stdin antes de leerlo; no es un error del sandbox.
        }
    }

    /**
     * Lee un flujo capturando como máximo {@code maxBytes} y descarta el
     * resto para no bloquear al proceso hijo.
     */
    static String readCapped(InputStream in, int maxBytes) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        byte[] buffer = new byte[Math.min(4096, Math.max(1, maxBytes))];
        int total = 0;
        try {
            while (total < maxBytes) {
                int read = in.read(buffer, 0, Math.min(buffer.length, maxBytes - total));
                if (read < 0) {
                    return captured.toString(StandardCharsets.UTF_8);
                }
                captured.write(buffer, 0, read);
                total += read;
            }
            byte[] sink = new byte[4096];
            while (in.read(sink) > 0) {
                // Descartado deliberadamente: el tope ya se alcanzó.
            }
        } catch (IOException ignored) {
            // El proceso hijo pudo cerrar la tubería; devolvemos lo capturado.
        }
        return captured.toString(StandardCharsets.UTF_8);
    }

    private static String safeGet(Future<String> future) {
        try {
            return future.get(2, TimeUnit.SECONDS);
        } catch (Exception e) {
            return "";
        }
    }

    private static int elapsed(long startMs) {
        return (int) Math.min(Integer.MAX_VALUE, System.currentTimeMillis() - startMs);
    }
}
