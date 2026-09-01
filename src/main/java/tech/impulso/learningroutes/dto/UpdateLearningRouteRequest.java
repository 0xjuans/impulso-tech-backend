package tech.impulso.learningroutes.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;

/**
 * Datos que se pueden modificar de una ruta existente. Los valores
 * {@code null} indican que el campo no debe modificarse.
 *
 * <p>El estado no se modifica desde este DTO: existe un endpoint
 * dedicado para publicar o deshabilitar la ruta.</p>
 *
 * @param name                   nuevo nombre visible.
 * @param description            nueva descripción.
 * @param objective              nuevo objetivo.
 * @param coverImageUrl          nueva URL de imagen.
 * @param difficulty             nuevo nivel de dificultad.
 * @param estimatedDurationHours nueva duración estimada.
 * @param technologies           nueva lista de tecnologías.
 */
public record UpdateLearningRouteRequest(
        @Size(max = 150) String name,
        String description,
        String objective,
        @Size(max = 500) String coverImageUrl,
        DifficultyLevel difficulty,
        @Positive Integer estimatedDurationHours,
        @Size(max = 300) String technologies
) {
}
