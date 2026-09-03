package tech.impulso.labs.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.ContentStatus;

/**
 * Datos necesarios para crear o actualizar un laboratorio (RF-013).
 *
 * @param title              título visible.
 * @param description        descripción general.
 * @param instructions       instrucciones detalladas para el estudiante.
 * @param language           lenguaje de programación soportado.
 * @param starterCode        código inicial sugerido, opcional.
 * @param expectedOutput     salida esperada para autocorrección, opcional.
 * @param executionTimeoutMs tiempo máximo permitido por ejecución.
 * @param courseId           curso al que pertenece el laboratorio, opcional.
 * @param lessonId           lección asociada, opcional.
 * @param status             estado del contenido tras la operación.
 */
public record LabRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank String description,
        @NotBlank String instructions,
        @NotBlank @Size(max = 60) String language,
        String starterCode,
        String expectedOutput,
        @Min(500) @Max(30_000) int executionTimeoutMs,
        Long courseId,
        Long lessonId,
        ContentStatus status
) {
}
