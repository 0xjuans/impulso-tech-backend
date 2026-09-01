package tech.impulso.evaluations.dto;

import tech.impulso.activities.entity.ActivityType;
import tech.impulso.evaluations.entity.EvaluationQuestion;

/**
 * Representación pública de una pregunta de evaluación.
 *
 * @param id           identificador de la pregunta.
 * @param orderIndex   posición dentro de la evaluación.
 * @param type         tipo de la pregunta.
 * @param questionText enunciado de la pregunta.
 * @param score        puntaje asignado.
 * @param config       configuración específica del tipo, ya adaptada al
 *                     consumidor (sin respuestas correctas cuando se
 *                     entrega a un estudiante).
 */
public record EvaluationQuestionResponse(
        Long id,
        Integer orderIndex,
        ActivityType type,
        String questionText,
        int score,
        String config
) {

    /**
     * Construye la representación pública a partir de la entidad.
     *
     * @param question     entidad a convertir.
     * @param configForApi configuración adaptada al consumidor.
     * @return DTO listo para devolver desde la API.
     */
    public static EvaluationQuestionResponse from(EvaluationQuestion question, String configForApi) {
        return new EvaluationQuestionResponse(
                question.getId(),
                question.getOrderIndex(),
                question.getType(),
                question.getQuestionText(),
                question.getScore(),
                configForApi
        );
    }
}
