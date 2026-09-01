package tech.impulso.evaluations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tech.impulso.activities.entity.ActivityType;

/**
 * Datos requeridos para crear o reemplazar una pregunta dentro de una
 * evaluación.
 *
 * @param orderIndex   posición dentro de la evaluación; se autoasigna si es nulo.
 * @param type         tipo de pregunta.
 * @param questionText enunciado de la pregunta.
 * @param score        puntaje asignado a la pregunta (mayor que cero).
 * @param config       configuración específica del tipo, en JSON, con la respuesta correcta.
 */
public record CreateEvaluationQuestionRequest(
        @Positive(message = "La posición debe ser mayor que cero.")
        Integer orderIndex,

        @NotNull(message = "El tipo de pregunta es obligatorio.")
        ActivityType type,

        @NotBlank(message = "El enunciado de la pregunta es obligatorio.")
        String questionText,

        @NotNull(message = "El puntaje es obligatorio.")
        @Positive(message = "El puntaje debe ser mayor que cero.")
        Integer score,

        @NotBlank(message = "La configuración de la pregunta es obligatoria.")
        String config
) {
}
