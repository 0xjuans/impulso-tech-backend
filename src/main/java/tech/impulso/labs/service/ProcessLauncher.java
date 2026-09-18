package tech.impulso.labs.service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstracción sobre el lanzamiento de procesos externos para el sandbox
 * de ejecución de código.
 *
 * <p>Su existencia permite sustituir el motor real de {@link ProcessBuilder}
 * por una implementación en memoria durante las pruebas del
 * {@link DockerCliCodeExecutionService}, sin depender de la existencia
 * del binario {@code docker} en el entorno de test.</p>
 */
public interface ProcessLauncher {

    /**
     * Ejecuta un proceso externo y devuelve su resultado.
     *
     * @param command        línea de comando completa (argv).
     * @param workingDir     directorio de trabajo del proceso.
     * @param stdin          entrada estándar opcional que se escribe al
     *                       iniciar el proceso.
     * @param timeoutMs      tiempo máximo de espera antes de matar el
     *                       proceso.
     * @param maxOutputBytes bytes máximos capturados por cada flujo
     *                       (stdout y stderr).
     * @return resultado con salida capturada, código de salida, flag de
     *         timeout y duración observada.
     * @throws IOException si el proceso no puede iniciarse.
     */
    ProcessOutcome run(List<String> command,
                       Path workingDir,
                       String stdin,
                       long timeoutMs,
                       int maxOutputBytes) throws IOException;

    /**
     * Resultado observable de un proceso ejecutado por
     * {@link ProcessLauncher}.
     *
     * @param stdout      salida estándar capturada (posiblemente truncada).
     * @param stderr      salida de error capturada (posiblemente truncada).
     * @param exitCode    código de salida, o {@code null} si el proceso
     *                    no llegó a terminar por sus propios medios.
     * @param timedOut    {@code true} cuando la ejecución excedió el
     *                    tiempo permitido y fue detenida.
     * @param durationMs  tiempo total desde el arranque hasta la
     *                    terminación o el kill forzado.
     */
    record ProcessOutcome(String stdout,
                          String stderr,
                          Integer exitCode,
                          boolean timedOut,
                          int durationMs) {
    }
}
