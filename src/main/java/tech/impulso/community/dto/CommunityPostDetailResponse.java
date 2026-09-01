package tech.impulso.community.dto;

import tech.impulso.community.entity.CommunityPost;
import tech.impulso.community.entity.CommunityReply;

import java.util.List;

/**
 * Representación detallada de una publicación que incluye la lista
 * completa de respuestas ordenadas cronológicamente.
 *
 * @param post    representación resumida de la publicación.
 * @param replies respuestas asociadas.
 */
public record CommunityPostDetailResponse(
        CommunityPostResponse post,
        List<CommunityReplyResponse> replies
) {

    /**
     * Construye la representación detallada a partir de la entidad y de
     * la lista de respuestas ya cargadas.
     */
    public static CommunityPostDetailResponse from(CommunityPost post, List<CommunityReply> replies) {
        Long acceptedReplyId = post.getAcceptedReplyId();
        List<CommunityReplyResponse> mapped = replies.stream()
                .map(reply -> CommunityReplyResponse.from(reply, acceptedReplyId))
                .toList();
        return new CommunityPostDetailResponse(CommunityPostResponse.from(post), mapped);
    }
}
