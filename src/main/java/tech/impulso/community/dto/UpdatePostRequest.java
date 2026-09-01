package tech.impulso.community.dto;

import jakarta.validation.constraints.Size;

/**
 * Datos que se pueden modificar de una publicación existente. Los
 * valores {@code null} indican que el campo no debe actualizarse.
 *
 * @param title       nuevo título.
 * @param description nueva descripción.
 * @param codeSnippet nuevo fragmento de código adjunto.
 * @param tags        nuevas etiquetas en formato CSV.
 */
public record UpdatePostRequest(
        @Size(max = 200) String title,
        String description,
        String codeSnippet,
        @Size(max = 500) String tags
) {
}
