package tech.impulso.labs.service;

/**
 * Puerta de entrada al servicio de ejecución de código de estudiantes
 * (RF-037).
 *
 * <p>El backend principal nunca ejecuta código de estudiantes en su
 * propio proceso. Toda ejecución se delega a un componente externo
 * aislado — típicamente un contenedor Docker con límites estrictos de
 * CPU, memoria, tiempo, procesos, red y sistema de archivos (RF-030,
 * §30 del CLAUDE.md).</p>
 *
 * <p>Esta interfaz permite conectar la implementación real cuando el
 * sandbox esté disponible sin modificar la lógica de laboratorios. La
 * implementación por defecto es un stub que rechaza toda ejecución de
 * manera explícita.</p>
 */
public interface CodeExecutionService {

    /**
     * Ejecuta el código suministrado y devuelve el resultado.
     *
     * @param request datos de la solicitud (lenguaje, código, límites).
     * @return resultado con salida estándar, error estándar, tiempo,
     *         código de salida y bandera de éxito.
     */
    ExecutionResult execute(ExecutionRequest request);

    /**
     * Datos de una solicitud de ejecución.
     *
     * @param language  identificador normalizado del lenguaje
     *                  (por ejemplo, {@code python}, {@code java}).
     * @param code      código a ejecutar.
     * @param stdin     entrada estándar opcional que se pasará al proceso.
     * @param timeoutMs tiempo máximo de ejecución en milisegundos.
     */
    record ExecutionRequest(String language, String code, String stdin, int timeoutMs) {
    }

    /**
     * Resultado devuelto por el sandbox.
     *
     * @param stdout          salida estándar capturada, ya recortada al
     *                        límite de longitud aplicable.
     * @param stderr          salida de error capturada.
     * @param exitCode        código de salida del proceso, o {@code null}
     *                        si la ejecución no llegó a iniciarse.
     * @param executionTimeMs duración total de la ejecución.
     * @param success         indica si la ejecución terminó sin errores
     *                        del sandbox (no implica que la lógica sea
     *                        correcta, sólo que el proceso corrió).
     * @param message         mensaje informativo del sandbox, útil para
     *                        reportar bloqueos, timeouts o indisponibilidad.
     */
    record ExecutionResult(String stdout,
                           String stderr,
                           Integer exitCode,
                           Integer executionTimeMs,
                           boolean success,
                           String message) {
    }
}
