package tech.impulso.community.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.community.entity.CommunityReply;

import java.util.List;

/**
 * Repositorio de persistencia para {@link CommunityReply}.
 */
@Repository
public interface CommunityReplyRepository extends JpaRepository<CommunityReply, Long> {

    /**
     * Devuelve las respuestas de una publicación ordenadas por su fecha
     * de creación (más antiguas primero).
     *
     * @param postId identificador de la publicación.
     * @return respuestas de la publicación.
     */
    List<CommunityReply> findByPostIdOrderByCreatedAtAsc(Long postId);
}
