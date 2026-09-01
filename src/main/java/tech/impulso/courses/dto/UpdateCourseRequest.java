package tech.impulso.courses.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;

/**
 * Datos que se pueden modificar de un curso existente. Los valores
 * {@code null} indican que el campo no debe actualizarse.
 *
 * @param name                   nuevo nombre.
 * @param description            nueva descripción.
 * @param objective              nuevo objetivo de aprendizaje.
 * @param coverImageUrl          nueva URL de la imagen representativa.
 * @param difficulty             nuevo nivel de dificultad.
 * @param estimatedDurationHours nueva duración estimada.
 * @param technology             nueva tecnología principal.
 * @param learningRouteId        identificador de la ruta a la que debe
 *                               pertenecer el curso; utilizar {@code -1}
 *                               para desasociar el curso de cualquier
 *                               ruta.
 * @param generatesCertificate   indica si el curso emite certificado.
 */
public record UpdateCourseRequest(
        @Size(max = 150) String name,
        String description,
        String objective,
        @Size(max = 500) String coverImageUrl,
        DifficultyLevel difficulty,
        @Positive Integer estimatedDurationHours,
        @Size(max = 120) String technology,
        Long learningRouteId,
        Boolean generatesCertificate
) {
}
