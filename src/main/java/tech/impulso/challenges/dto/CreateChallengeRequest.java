package tech.impulso.challenges.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;

/**
 * Datos requeridos para crear un nuevo reto de programación.
 *
 * @param allowedLanguages lista de lenguajes permitidos en formato CSV.
 */
public record CreateChallengeRequest(
        @NotBlank(message = "El nombre del reto es obligatorio.")
        @Size(max = 180)
        String name,

        @NotBlank(message = "La descripción es obligatoria.")
        String description,

        String objective,
        String instructions,

        @NotNull(message = "El nivel de dificultad es obligatorio.")
        DifficultyLevel difficulty,

        @NotBlank(message = "Debe especificar al menos un lenguaje permitido.")
        @Size(max = 300)
        String allowedLanguages,

        String ioExamples,
        String restrictions,
        String publicTestCases,
        String hiddenTestCases,

        @PositiveOrZero(message = "La XP otorgada no puede ser negativa.")
        Integer xpReward,

        @Positive(message = "El tiempo estimado debe ser mayor que cero.")
        Integer estimatedMinutes,

        Long learningRouteId,
        Long courseId,
        Long moduleId,
        Long lessonId
) {
}
