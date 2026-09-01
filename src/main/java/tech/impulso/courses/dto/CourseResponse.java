package tech.impulso.courses.dto;

import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.courses.entity.Course;

import java.time.OffsetDateTime;

/**
 * Representación pública de un curso.
 *
 * @param id                     identificador interno del curso.
 * @param name                   nombre visible.
 * @param description            descripción general.
 * @param objective              objetivo de aprendizaje.
 * @param coverImageUrl          URL pública de la imagen representativa.
 * @param difficulty             nivel de dificultad.
 * @param estimatedDurationHours duración estimada en horas.
 * @param technology             tecnología o lenguaje principal.
 * @param status                 estado del ciclo de vida.
 * @param generatesCertificate   indica si el curso emite certificado.
 * @param learningRouteId        identificador de la ruta asociada, si aplica.
 * @param learningRouteName      nombre de la ruta asociada, si aplica.
 * @param instructorId           identificador del instructor responsable.
 * @param instructorFullName     nombre completo del instructor.
 * @param createdAt              fecha de creación.
 * @param updatedAt              fecha de la última modificación.
 */
public record CourseResponse(
        Long id,
        String name,
        String description,
        String objective,
        String coverImageUrl,
        DifficultyLevel difficulty,
        Integer estimatedDurationHours,
        String technology,
        ContentStatus status,
        boolean generatesCertificate,
        Long learningRouteId,
        String learningRouteName,
        Long instructorId,
        String instructorFullName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param course entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static CourseResponse from(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getObjective(),
                course.getCoverImageUrl(),
                course.getDifficulty(),
                course.getEstimatedDurationHours(),
                course.getTechnology(),
                course.getStatus(),
                course.isGeneratesCertificate(),
                course.getLearningRoute() == null ? null : course.getLearningRoute().getId(),
                course.getLearningRoute() == null ? null : course.getLearningRoute().getName(),
                course.getInstructor().getId(),
                "%s %s".formatted(course.getInstructor().getFirstName(), course.getInstructor().getLastName()),
                course.getCreatedAt(),
                course.getUpdatedAt()
        );
    }
}
