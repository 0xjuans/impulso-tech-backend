package tech.impulso.challenges.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;

/**
 * Datos que se pueden modificar de un reto existente. Los valores
 * {@code null} indican que el campo no debe actualizarse. Para
 * desasociar el reto de una ruta, curso, módulo o lección se puede
 * enviar {@code -1} en el identificador correspondiente.
 */
public record UpdateChallengeRequest(
        @Size(max = 180) String name,
        String description,
        String objective,
        String instructions,
        DifficultyLevel difficulty,
        @Size(max = 300) String allowedLanguages,
        String ioExamples,
        String restrictions,
        String publicTestCases,
        String hiddenTestCases,
        @PositiveOrZero Integer xpReward,
        @Positive Integer estimatedMinutes,
        Long learningRouteId,
        Long courseId,
        Long moduleId,
        Long lessonId
) {
}
