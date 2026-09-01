package tech.impulso.community.dto;

import tech.impulso.community.entity.CommunityReply;

import java.time.OffsetDateTime;

/**
 * Representación pública de una respuesta de la comunidad.
 *
 * @param id           identificador de la respuesta.
 * @param postId       identificador de la publicación.
 * @param authorId     identificador del autor.
 * @param authorName   nombre completo del autor.
 * @param content      contenido textual.
 * @param codeSnippet  fragmento de código adjunto, si aplica.
 * @param helpfulCount cantidad de votos "útil" recibidos.
 * @param accepted     indica si esta respuesta fue aceptada por el autor de la publicación.
 * @param createdAt    fecha de creación.
 * @param updatedAt    fecha de última modificación.
 */
public record CommunityReplyResponse(
        Long id,
        Long postId,
        Long authorId,
        String authorName,
        String content,
        String codeSnippet,
        int helpfulCount,
        boolean accepted,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir de la entidad y del
     * identificador de la respuesta aceptada (proveniente del post).
     */
    public static CommunityReplyResponse from(CommunityReply reply, Long acceptedReplyId) {
        return new CommunityReplyResponse(
                reply.getId(),
                reply.getPost().getId(),
                reply.getAuthor().getId(),
                "%s %s".formatted(reply.getAuthor().getFirstName(), reply.getAuthor().getLastName()),
                reply.getContent(),
                reply.getCodeSnippet(),
                reply.getHelpfulCount(),
                acceptedReplyId != null && acceptedReplyId.equals(reply.getId()),
                reply.getCreatedAt(),
                reply.getUpdatedAt()
        );
    }
}
