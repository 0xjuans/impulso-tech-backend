package tech.impulso.courses.dto;

import tech.impulso.common.content.ContentStatus;
import tech.impulso.courses.entity.CourseModule;

import java.time.OffsetDateTime;

/**
 * Representación pública de un módulo de curso.
 *
 * @param id          identificador interno del módulo.
 * @param courseId    identificador del curso al que pertenece.
 * @param name        nombre visible.
 * @param description descripción general.
 * @param objective   objetivo de aprendizaje.
 * @param orderIndex  posición dentro del curso.
 * @param optional    indica si el módulo es opcional.
 * @param status      estado del ciclo de vida.
 * @param createdAt   fecha de creación.
 * @param updatedAt   fecha de la última modificación.
 */
public record CourseModuleResponse(
        Long id,
        Long courseId,
        String name,
        String description,
        String objective,
        Integer orderIndex,
        boolean optional,
        ContentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param module entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static CourseModuleResponse from(CourseModule module) {
        return new CourseModuleResponse(
                module.getId(),
                module.getCourse().getId(),
                module.getName(),
                module.getDescription(),
                module.getObjective(),
                module.getOrderIndex(),
                module.isOptional(),
                module.getStatus(),
                module.getCreatedAt(),
                module.getUpdatedAt()
        );
    }
}
