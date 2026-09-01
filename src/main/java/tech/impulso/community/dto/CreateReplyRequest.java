package tech.impulso.community.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Datos requeridos para publicar una respuesta a una publicación de la
 * comunidad.
 *
 * @param content     contenido textual de la respuesta.
 * @param codeSnippet fragmento de código adjunto, opcional.
 */
public record CreateReplyRequest(
        @NotBlank(message = "El contenido de la respuesta es obligatorio.")
        String content,

        String codeSnippet
) {
}
