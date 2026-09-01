package tech.impulso.resources.dto;

import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.resources.entity.ResourceType;

/**
 * Datos que se pueden modificar de un recurso existente. Los valores
 * {@code null} indican que el campo no debe actualizarse.
 *
 * <p>Para desasociar el recurso de una ruta, curso, módulo o lección se
 * puede enviar el valor {@code -1} en el identificador correspondiente.</p>
 *
 * @param name             nuevo nombre.
 * @param description      nueva descripción.
 * @param type             nuevo tipo.
 * @param category         nueva categoría.
 * @param topic            nuevo tema.
 * @param technology       nueva tecnología.
 * @param difficulty       nuevo nivel de dificultad.
 * @param author           nuevo autor.
 * @param resourceUrl      nueva URL del recurso.
 * @param learningRouteId  nueva ruta asociada (o -1 para desasociar).
 * @param courseId         nuevo curso asociado (o -1 para desasociar).
 * @param moduleId         nuevo módulo asociado (o -1 para desasociar).
 * @param lessonId         nueva lección asociada (o -1 para desasociar).
 */
public record UpdateResourceRequest(
        @Size(max = 180) String name,
        String description,
        ResourceType type,
        @Size(max = 120) String category,
        @Size(max = 150) String topic,
        @Size(max = 120) String technology,
        DifficultyLevel difficulty,
        @Size(max = 150) String author,
        @Size(max = 1000) String resourceUrl,
        Long learningRouteId,
        Long courseId,
        Long moduleId,
        Long lessonId
) {
}
