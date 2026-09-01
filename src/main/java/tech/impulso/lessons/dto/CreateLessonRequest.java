package tech.impulso.lessons.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos requeridos para crear una nueva lección dentro de un módulo.
 *
 * <p>Cuando {@code orderIndex} es nulo el sistema asigna
 * automáticamente la siguiente posición disponible dentro del módulo.</p>
 *
 * @param title                    título visible de la lección.
 * @param description              descripción breve.
 * @param objective                objetivo de aprendizaje.
 * @param content                  contenido educativo en Markdown o HTML.
 * @param estimatedDurationMinutes duración estimada en minutos.
 * @param orderIndex               posición explícita dentro del módulo.
 * @param optional                 indica si la lección es opcional.
 */
public record CreateLessonRequest(
        @NotBlank(message = "El título de la lección es obligatorio.")
        @Size(max = 180)
        String title,

        String description,

        String objective,

        String content,

        @Positive(message = "La duración estimada debe ser mayor que cero.")
        Integer estimatedDurationMinutes,

        @Positive(message = "La posición debe ser mayor que cero.")
        Integer orderIndex,

        boolean optional
) {
}
