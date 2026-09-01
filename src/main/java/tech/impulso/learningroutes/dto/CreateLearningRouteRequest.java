package tech.impulso.learningroutes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;

/**
 * Datos requeridos para crear una nueva ruta de aprendizaje.
 *
 * <p>La ruta se crea en estado {@code BORRADOR} y solo será visible
 * para los estudiantes cuando el instructor decida publicarla.</p>
 *
 * @param name                   nombre visible.
 * @param description            descripción general.
 * @param objective              objetivo de aprendizaje.
 * @param coverImageUrl          URL pública de la imagen representativa.
 * @param difficulty             nivel de dificultad.
 * @param estimatedDurationHours duración estimada en horas.
 * @param technologies           tecnologías relacionadas (texto separado por comas).
 */
public record CreateLearningRouteRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 150)
        String name,

        @NotBlank(message = "La descripción es obligatoria.")
        String description,

        String objective,

        @Size(max = 500)
        String coverImageUrl,

        @NotNull(message = "El nivel de dificultad es obligatorio.")
        DifficultyLevel difficulty,

        @Positive(message = "La duración estimada debe ser mayor que cero.")
        Integer estimatedDurationHours,

        @Size(max = 300)
        String technologies
) {
}
