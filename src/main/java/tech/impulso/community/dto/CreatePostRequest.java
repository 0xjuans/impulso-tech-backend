package tech.impulso.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.impulso.community.entity.RelatedContentType;

/**
 * Datos requeridos para crear una nueva publicación en la comunidad.
 *
 * @param title        título breve.
 * @param description  descripción o pregunta desarrollada.
 * @param codeSnippet  fragmento de código adjunto, opcional.
 * @param tags         etiquetas asociadas en formato CSV.
 * @param relatedType  tipo del contenido educativo relacionado, opcional.
 * @param relatedId    identificador del contenido educativo relacionado, opcional.
 */
public record CreatePostRequest(
        @NotBlank(message = "El título es obligatorio.")
        @Size(max = 200)
        String title,

        @NotBlank(message = "La descripción es obligatoria.")
        String description,

        String codeSnippet,

        @Size(max = 500)
        String tags,

        RelatedContentType relatedType,

        Long relatedId
) {
}
