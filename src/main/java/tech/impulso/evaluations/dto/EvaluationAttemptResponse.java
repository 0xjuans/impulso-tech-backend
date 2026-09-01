package tech.impulso.evaluations.dto;

import tech.impulso.evaluations.entity.EvaluationAttempt;

import java.time.OffsetDateTime;

/**
 * Representación pública de un intento de evaluación.
 *
 * @param id               identificador del intento.
 * @param evaluationId     identificador de la evaluación.
 * @param startedAt        momento en que se inició el intento.
 * @param finishedAt       momento en que se finalizó el intento.
 * @param totalScore       puntaje obtenido.
 * @param maxPossibleScore puntaje máximo posible.
 * @param percentage       porcentaje de acierto (0 a 100).
 * @param passed           indica si el intento se considera aprobado.
 */
public record EvaluationAttemptResponse(
        Long id,
        Long evaluationId,
        OffsetDateTime startedAt,
        OffsetDateTime finishedAt,
        int totalScore,
        int maxPossibleScore,
        int percentage,
        boolean passed
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param attempt intento a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static EvaluationAttemptResponse from(EvaluationAttempt attempt) {
        return new EvaluationAttemptResponse(
                attempt.getId(),
                attempt.getEvaluation().getId(),
                attempt.getStartedAt(),
                attempt.getFinishedAt(),
                attempt.getTotalScore(),
                attempt.getMaxPossibleScore(),
                attempt.getPercentage(),
                attempt.isPassed()
        );
    }
}
