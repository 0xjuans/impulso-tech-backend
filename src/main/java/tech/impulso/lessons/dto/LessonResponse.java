package tech.impulso.lessons.dto;

import tech.impulso.common.content.ContentStatus;
import tech.impulso.lessons.entity.Lesson;

import java.time.OffsetDateTime;

/**
 * Representación pública de una lección.
 *
 * @param id                       identificador interno.
 * @param moduleId                 identificador del módulo al que pertenece.
 * @param title                    título visible.
 * @param description              descripción breve.
 * @param objective                objetivo de aprendizaje.
 * @param content                  contenido educativo en Markdown o HTML.
 * @param estimatedDurationMinutes duración estimada en minutos.
 * @param orderIndex               posición dentro del módulo.
 * @param optional                 indica si la lección es opcional.
 * @param status                   estado del ciclo de vida.
 * @param createdAt                fecha de creación.
 * @param updatedAt                fecha de la última modificación.
 */
public record LessonResponse(
        Long id,
        Long moduleId,
        String title,
        String description,
        String objective,
        String content,
        Integer estimatedDurationMinutes,
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
     * @param lesson entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static LessonResponse from(Lesson lesson) {
        return new LessonResponse(
                lesson.getId(),
                lesson.getModule().getId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getObjective(),
                lesson.getContent(),
                lesson.getEstimatedDurationMinutes(),
                lesson.getOrderIndex(),
                lesson.isOptional(),
                lesson.getStatus(),
                lesson.getCreatedAt(),
                lesson.getUpdatedAt()
        );
    }
}
