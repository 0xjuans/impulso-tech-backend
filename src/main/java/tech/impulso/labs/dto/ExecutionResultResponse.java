package tech.impulso.labs.dto;

import tech.impulso.labs.service.CodeExecutionService;

/**
 * Representación pública del resultado de una ejecución (RF-037).
 *
 * @param stdout          salida estándar capturada.
 * @param stderr          salida de error capturada.
 * @param exitCode        código de salida del proceso.
 * @param executionTimeMs duración total de la ejecución.
 * @param success         indica si el proceso corrió sin fallos del
 *                        sandbox.
 * @param passed          indica si además la salida coincide con la
 *                        esperada por el laboratorio (cuando aplica).
 * @param message         mensaje informativo del sandbox.
 */
public record ExecutionResultResponse(
        String stdout,
        String stderr,
        Integer exitCode,
        Integer executionTimeMs,
        boolean success,
        boolean passed,
        String message
) {

    public static ExecutionResultResponse from(CodeExecutionService.ExecutionResult result, boolean passed) {
        return new ExecutionResultResponse(
                result.stdout(),
                result.stderr(),
                result.exitCode(),
                result.executionTimeMs(),
                result.success(),
                passed,
                result.message()
        );
    }
}
