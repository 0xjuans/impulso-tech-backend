package tech.impulso.lessons.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos que se pueden modificar de una lección existente. Los valores
 * {@code null} indican que el campo no debe actualizarse.
 *
 * @param title                    nuevo título.
 * @param description              nueva descripción.
 * @param objective                nuevo objetivo.
 * @param content                  nuevo contenido educativo.
 * @param estimatedDurationMinutes nueva duración estimada.
 * @param orderIndex               nueva posición dentro del módulo.
 * @param optional                 indica si la lección pasa a ser opcional.
 */
public record UpdateLessonRequest(
        @Size(max = 180) String title,
        String description,
        String objective,
        String content,
        @Positive Integer estimatedDurationMinutes,
        @Positive Integer orderIndex,
        Boolean optional
) {
}
