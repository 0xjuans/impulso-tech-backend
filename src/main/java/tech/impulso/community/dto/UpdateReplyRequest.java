package tech.impulso.community.dto;

/**
 * Datos que se pueden modificar de una respuesta existente. Los valores
 * {@code null} indican que el campo no debe actualizarse.
 *
 * @param content     nuevo contenido textual.
 * @param codeSnippet nuevo fragmento de código adjunto.
 */
public record UpdateReplyRequest(
        String content,
        String codeSnippet
) {
}
