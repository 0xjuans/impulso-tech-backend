package tech.impulso.evaluations.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos requeridos para crear una nueva evaluación dentro de una
 * lección. Las preguntas se agregan posteriormente mediante el endpoint
 * dedicado.
 *
 * @param name               nombre visible.
 * @param description        descripción general.
 * @param instructions       instrucciones detalladas.
 * @param timeLimitMinutes   tiempo máximo permitido en minutos (nulo = sin límite).
 * @param passingPercentage  porcentaje mínimo requerido para aprobar (0 a 100).
 * @param maxAttempts        cantidad máxima de intentos.
 * @param orderIndex         posición dentro de la lección; se autoasigna si es nulo.
 */
public record CreateEvaluationRequest(
        @NotBlank(message = "El nombre de la evaluación es obligatorio.")
        @Size(max = 180)
        String name,

        String description,

        String instructions,

        @Positive(message = "El tiempo límite debe ser mayor que cero.")
        Integer timeLimitMinutes,

        @Min(value = 0, message = "El porcentaje mínimo no puede ser negativo.")
        @Max(value = 100, message = "El porcentaje mínimo no puede superar 100.")
        Integer passingPercentage,

        @Positive(message = "El número máximo de intentos debe ser mayor que cero.")
        Integer maxAttempts,

        @Positive(message = "La posición debe ser mayor que cero.")
        Integer orderIndex
) {
}
