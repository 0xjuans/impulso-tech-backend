package tech.impulso.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;

import java.time.OffsetDateTime;

/**
 * Datos requeridos para crear un nuevo proyecto.
 */
public record CreateProjectRequest(
        @NotBlank(message = "El nombre del proyecto es obligatorio.")
        @Size(max = 180)
        String name,

        @NotBlank(message = "La descripción es obligatoria.")
        String description,

        String objective,
        String instructions,
        String requirements,

        @NotNull(message = "El nivel de dificultad es obligatorio.")
        DifficultyLevel difficulty,

        @Size(max = 300) String technologies,
        String resources,
        String evaluationCriteria,

        @Positive(message = "El puntaje máximo debe ser mayor que cero.")
        Integer maxScore,

        @PositiveOrZero(message = "La XP otorgada no puede ser negativa.")
        Integer xpReward,

        OffsetDateTime deadlineAt,

        Long learningRouteId,
        Long courseId,
        Long moduleId
) {
}
