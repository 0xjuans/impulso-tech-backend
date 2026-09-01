package tech.impulso.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.community.entity.CommunityHelpfulVote;

import java.util.Optional;

/**
 * Repositorio de persistencia para {@link CommunityHelpfulVote}.
 */
@Repository
public interface CommunityHelpfulVoteRepository extends JpaRepository<CommunityHelpfulVote, Long> {

    /**
     * Localiza el voto de un usuario sobre una respuesta, si existe.
     *
     * @param replyId identificador de la respuesta.
     * @param userId  identificador del usuario.
     * @return voto existente si el usuario ya la marcó.
     */
    Optional<CommunityHelpfulVote> findByReplyIdAndUserId(Long replyId, Long userId);
}
