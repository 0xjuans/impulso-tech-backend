package tech.impulso.labs.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Implementación por defecto de {@link CodeExecutionService} (RF-037).
 *
 * <p>Rechaza toda solicitud de ejecución con un mensaje explícito de
 * indisponibilidad, ya que ejecutar código de estudiantes dentro del
 * proceso principal violaría las reglas de aislamiento (§30 del
 * CLAUDE.md). Este componente se sustituye por la integración real con
 * el sandbox tan pronto como esté disponible.</p>
 *
 * <p>Se registra únicamente cuando no existe otro bean de
 * {@link CodeExecutionService} en el contexto.</p>
 */
@Component
@ConditionalOnMissingBean(value = CodeExecutionService.class,
        ignored = StubCodeExecutionService.class)
public class StubCodeExecutionService implements CodeExecutionService {

    @Override
    public ExecutionResult execute(ExecutionRequest request) {
        return new ExecutionResult(
                "",
                "",
                null,
                0,
                false,
                "El servicio de ejecución de código no está disponible en este ambiente. "
                        + "Configure un sandbox aislado que implemente CodeExecutionService."
        );
    }
}
