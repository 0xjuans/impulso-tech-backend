package tech.impulso.learningroutes.dto;

import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.learningroutes.entity.LearningRoute;

import java.time.OffsetDateTime;

/**
 * Representación pública de una ruta de aprendizaje.
 *
 * @param id                     identificador interno de la ruta.
 * @param name                   nombre visible.
 * @param description            descripción general.
 * @param objective              objetivo de aprendizaje.
 * @param coverImageUrl          URL de la imagen o recurso visual.
 * @param difficulty             nivel de dificultad.
 * @param estimatedDurationHours duración estimada en horas.
 * @param technologies           tecnologías o lenguajes relacionados.
 * @param status                 estado del ciclo de vida.
 * @param instructorId           identificador del instructor responsable.
 * @param instructorFullName     nombre completo del instructor.
 * @param createdAt              fecha de creación.
 * @param updatedAt              fecha de la última modificación.
 */
public record LearningRouteResponse(
        Long id,
        String name,
        String description,
        String objective,
        String coverImageUrl,
        DifficultyLevel difficulty,
        Integer estimatedDurationHours,
        String technologies,
        ContentStatus status,
        Long instructorId,
        String instructorFullName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param route entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static LearningRouteResponse from(LearningRoute route) {
        return new LearningRouteResponse(
                route.getId(),
                route.getName(),
                route.getDescription(),
                route.getObjective(),
                route.getCoverImageUrl(),
                route.getDifficulty(),
                route.getEstimatedDurationHours(),
                route.getTechnologies(),
                route.getStatus(),
                route.getInstructor().getId(),
                "%s %s".formatted(route.getInstructor().getFirstName(), route.getInstructor().getLastName()),
                route.getCreatedAt(),
                route.getUpdatedAt()
        );
    }
}
