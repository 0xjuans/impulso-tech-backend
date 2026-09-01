package tech.impulso.courses.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos requeridos para crear un nuevo módulo dentro de un curso.
 *
 * <p>Cuando {@code orderIndex} es nulo el sistema asigna
 * automáticamente la siguiente posición disponible en el curso.</p>
 *
 * @param name        nombre visible del módulo.
 * @param description descripción general.
 * @param objective   objetivo de aprendizaje.
 * @param orderIndex  posición explícita dentro del curso.
 * @param optional    indica si el módulo es opcional.
 */
public record CreateCourseModuleRequest(
        @NotBlank(message = "El nombre del módulo es obligatorio.")
        @Size(max = 150)
        String name,

        String description,

        String objective,

        @Positive(message = "La posición del módulo debe ser mayor que cero.")
        Integer orderIndex,

        boolean optional
) {
}
