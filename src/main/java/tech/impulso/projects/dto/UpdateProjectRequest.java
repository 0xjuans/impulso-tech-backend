package tech.impulso.projects.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;

import java.time.OffsetDateTime;

/**
 * Datos que se pueden modificar de un proyecto existente. Los valores
 * {@code null} indican que el campo no debe actualizarse.
 *
 * <p>Para desasociar el proyecto de una ruta, curso o módulo se puede
 * enviar {@code -1} en el identificador correspondiente.</p>
 */
public record UpdateProjectRequest(
        @Size(max = 180) String name,
        String description,
        String objective,
        String instructions,
        String requirements,
        DifficultyLevel difficulty,
        @Size(max = 300) String technologies,
        String resources,
        String evaluationCriteria,
        @Positive Integer maxScore,
        @PositiveOrZero Integer xpReward,
        OffsetDateTime deadlineAt,
        Long learningRouteId,
        Long courseId,
        Long moduleId
) {
}
