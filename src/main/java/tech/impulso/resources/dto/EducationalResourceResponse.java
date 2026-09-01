package tech.impulso.resources.dto;

import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.resources.entity.EducationalResource;
import tech.impulso.resources.entity.ResourceType;

import java.time.OffsetDateTime;

/**
 * Representación pública de un recurso educativo.
 *
 * @param id                identificador interno.
 * @param name              nombre visible.
 * @param description       descripción general.
 * @param type              tipo del recurso.
 * @param category          categoría temática.
 * @param topic             tema o subtema.
 * @param technology        tecnología o lenguaje relacionado.
 * @param difficulty        nivel de dificultad sugerido.
 * @param author            autor del contenido.
 * @param resourceUrl       URL del recurso.
 * @param status            estado del ciclo de vida.
 * @param publishedAt       fecha de publicación.
 * @param createdById       identificador del usuario que registró el recurso.
 * @param createdByName     nombre del usuario que registró el recurso.
 * @param learningRouteId   ruta asociada, si aplica.
 * @param courseId          curso asociado, si aplica.
 * @param moduleId          módulo asociado, si aplica.
 * @param lessonId          lección asociada, si aplica.
 * @param createdAt         fecha de creación del registro.
 * @param updatedAt         fecha de la última modificación.
 */
public record EducationalResourceResponse(
        Long id,
        String name,
        String description,
        ResourceType type,
        String category,
        String topic,
        String technology,
        DifficultyLevel difficulty,
        String author,
        String resourceUrl,
        ContentStatus status,
        OffsetDateTime publishedAt,
        Long createdById,
        String createdByName,
        Long learningRouteId,
        Long courseId,
        Long moduleId,
        Long lessonId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param resource entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static EducationalResourceResponse from(EducationalResource resource) {
        return new EducationalResourceResponse(
                resource.getId(),
                resource.getName(),
                resource.getDescription(),
                resource.getType(),
                resource.getCategory(),
                resource.getTopic(),
                resource.getTechnology(),
                resource.getDifficulty(),
                resource.getAuthor(),
                resource.getResourceUrl(),
                resource.getStatus(),
                resource.getPublishedAt(),
                resource.getCreatedBy().getId(),
                "%s %s".formatted(resource.getCreatedBy().getFirstName(), resource.getCreatedBy().getLastName()),
                resource.getLearningRoute() == null ? null : resource.getLearningRoute().getId(),
                resource.getCourse() == null ? null : resource.getCourse().getId(),
                resource.getModule() == null ? null : resource.getModule().getId(),
                resource.getLesson() == null ? null : resource.getLesson().getId(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}
