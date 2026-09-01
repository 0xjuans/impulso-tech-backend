package tech.impulso.courses.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos que se pueden modificar de un módulo existente. Los valores
 * {@code null} indican que el campo no debe actualizarse.
 *
 * @param name        nuevo nombre visible.
 * @param description nueva descripción.
 * @param objective   nuevo objetivo de aprendizaje.
 * @param orderIndex  nueva posición del módulo dentro del curso.
 * @param optional    indica si el módulo pasa a ser opcional.
 */
public record UpdateCourseModuleRequest(
        @Size(max = 150) String name,
        String description,
        String objective,
        @Positive Integer orderIndex,
        Boolean optional
) {
}
