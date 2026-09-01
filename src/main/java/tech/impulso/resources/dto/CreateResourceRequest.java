package tech.impulso.resources.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.resources.entity.ResourceType;

/**
 * Datos requeridos para crear un nuevo recurso educativo.
 *
 * @param name             nombre visible.
 * @param description      descripción general.
 * @param type             tipo funcional.
 * @param category         categoría temática.
 * @param topic            tema o subtema.
 * @param technology       tecnología o lenguaje relacionado.
 * @param difficulty       nivel de dificultad sugerido.
 * @param author           autor del contenido.
 * @param resourceUrl      URL del recurso.
 * @param learningRouteId  ruta asociada, si aplica.
 * @param courseId         curso asociado, si aplica.
 * @param moduleId         módulo asociado, si aplica.
 * @param lessonId         lección asociada, si aplica.
 */
public record CreateResourceRequest(
        @NotBlank(message = "El nombre del recurso es obligatorio.")
        @Size(max = 180)
        String name,

        String description,

        @NotNull(message = "El tipo del recurso es obligatorio.")
        ResourceType type,

        @Size(max = 120) String category,
        @Size(max = 150) String topic,
        @Size(max = 120) String technology,

        DifficultyLevel difficulty,

        @Size(max = 150) String author,

        @NotBlank(message = "La URL del recurso es obligatoria.")
        @Size(max = 1000)
        String resourceUrl,

        Long learningRouteId,
        Long courseId,
        Long moduleId,
        Long lessonId
) {
}
