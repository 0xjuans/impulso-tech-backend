package tech.impulso.evaluations.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Respuestas enviadas por el estudiante al finalizar una evaluación.
 *
 * <p>El campo {@code answers} es un objeto JSON cuyas claves son los
 * identificadores de pregunta y cuyos valores son el contenido de la
 * respuesta según el tipo (por ejemplo,
 * {@code {"12": {"selectedIndex": 1}, "13": {"answer": true}}}).</p>
 *
 * @param answers respuestas en formato JSON indexadas por pregunta.
 */
public record SubmitEvaluationRequest(
        @NotBlank(message = "Las respuestas son obligatorias.")
        String answers
) {
}
