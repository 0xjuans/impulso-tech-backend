package tech.impulso.community.dto;

import tech.impulso.community.entity.CommunityPost;
import tech.impulso.community.entity.RelatedContentType;

import java.time.OffsetDateTime;

/**
 * Representación resumida de una publicación de la comunidad, sin
 * incluir el detalle de las respuestas. Se utiliza en los listados.
 *
 * @param id              identificador de la publicación.
 * @param authorId        identificador del autor.
 * @param authorName      nombre completo del autor.
 * @param title           título breve.
 * @param description     descripción o pregunta desarrollada.
 * @param codeSnippet     fragmento de código adjunto, si aplica.
 * @param tags            etiquetas asociadas (CSV).
 * @param relatedType     tipo del contenido relacionado, si aplica.
 * @param relatedId       identificador del contenido relacionado, si aplica.
 * @param acceptedReplyId identificador de la respuesta aceptada, si aplica.
 * @param createdAt       fecha de creación.
 * @param updatedAt       fecha de última modificación.
 */
public record CommunityPostResponse(
        Long id,
        Long authorId,
        String authorName,
        String title,
        String description,
        String codeSnippet,
        String tags,
        RelatedContentType relatedType,
        Long relatedId,
        Long acceptedReplyId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir de la entidad.
     *
     * @param post entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static CommunityPostResponse from(CommunityPost post) {
        return new CommunityPostResponse(
                post.getId(),
                post.getAuthor().getId(),
                "%s %s".formatted(post.getAuthor().getFirstName(), post.getAuthor().getLastName()),
                post.getTitle(),
                post.getDescription(),
                post.getCodeSnippet(),
                post.getTags(),
                post.getRelatedType(),
                post.getRelatedId(),
                post.getAcceptedReplyId(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }
}
