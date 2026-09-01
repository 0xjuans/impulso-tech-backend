package tech.impulso.evaluations.dto;

import tech.impulso.common.content.ContentStatus;
import tech.impulso.evaluations.entity.Evaluation;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Representación pública de una evaluación.
 *
 * @param id                  identificador de la evaluación.
 * @param lessonId            identificador de la lección.
 * @param name                nombre visible.
 * @param description         descripción general.
 * @param instructions        instrucciones detalladas.
 * @param timeLimitMinutes    tiempo máximo permitido en minutos, si aplica.
 * @param passingPercentage   porcentaje mínimo requerido para aprobar.
 * @param maxAttempts         cantidad máxima de intentos.
 * @param orderIndex          posición dentro de la lección.
 * @param status              estado del ciclo de vida.
 * @param questions           preguntas asociadas a la evaluación.
 * @param maxPossibleScore    puntaje máximo obtenible en la evaluación.
 * @param createdAt           fecha de creación.
 * @param updatedAt           fecha de la última modificación.
 */
public record EvaluationResponse(
        Long id,
        Long lessonId,
        String name,
        String description,
        String instructions,
        Integer timeLimitMinutes,
        int passingPercentage,
        int maxAttempts,
        Integer orderIndex,
        ContentStatus status,
        List<EvaluationQuestionResponse> questions,
        int maxPossibleScore,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública combinando la evaluación y su
     * lista de preguntas.
     *
     * @param evaluation entidad a convertir.
     * @param questions  preguntas ya mapeadas.
     * @return DTO listo para devolver desde la API.
     */
    public static EvaluationResponse from(Evaluation evaluation, List<EvaluationQuestionResponse> questions) {
        int max = questions.stream().mapToInt(EvaluationQuestionResponse::score).sum();
        return new EvaluationResponse(
                evaluation.getId(),
                evaluation.getLesson().getId(),
                evaluation.getName(),
                evaluation.getDescription(),
                evaluation.getInstructions(),
                evaluation.getTimeLimitMinutes(),
                evaluation.getPassingPercentage(),
                evaluation.getMaxAttempts(),
                evaluation.getOrderIndex(),
                evaluation.getStatus(),
                questions,
                max,
                evaluation.getCreatedAt(),
                evaluation.getUpdatedAt()
        );
    }
}
