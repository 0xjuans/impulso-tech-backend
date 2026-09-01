package tech.impulso.courses.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;

/**
 * Datos requeridos para crear un nuevo curso.
 *
 * <p>El curso se crea en estado {@code BORRADOR}. La ruta de aprendizaje
 * es opcional: cuando se especifica, el curso pasa a formar parte de esa
 * ruta.</p>
 *
 * @param name                   nombre visible del curso.
 * @param description            descripción general.
 * @param objective              objetivo de aprendizaje.
 * @param coverImageUrl          URL pública de la imagen representativa.
 * @param difficulty             nivel de dificultad.
 * @param estimatedDurationHours duración estimada en horas.
 * @param technology             tecnología principal.
 * @param learningRouteId        identificador de la ruta asociada, si aplica.
 * @param generatesCertificate   indica si al completar el curso se emite
 *                               un certificado digital verificable.
 */
public record CreateCourseRequest(
        @NotBlank(message = "El nombre del curso es obligatorio.")
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

        @Size(max = 120)
        String technology,

        Long learningRouteId,

        boolean generatesCertificate
) {
}
