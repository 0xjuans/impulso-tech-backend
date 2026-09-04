package tech.impulso.messaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.messaging.entity.MessageConversation;

import java.util.Optional;

/**
 * Repositorio de persistencia para {@link MessageConversation}.
 */
@Repository
public interface MessageConversationRepository extends JpaRepository<MessageConversation, Long> {

    /**
     * Busca la conversación entre los dos usuarios indicados. Los
     * identificadores pueden pasarse en cualquier orden; el llamador
     * debe encargarse de normalizarlos antes de invocar métodos de
     * escritura.
     */
    Optional<MessageConversation> findByParticipantLowIdAndParticipantHighId(Long lowId, Long highId);

    /**
     * Devuelve las conversaciones en las que participa el usuario
     * indicado, ordenadas por el último mensaje intercambiado.
     */
    @Query("""
            select c from MessageConversation c
            where c.participantLow.id = :userId
               or c.participantHigh.id = :userId
            order by c.lastMessageAt desc
            """)
    Page<MessageConversation> findByParticipantId(@Param("userId") Long userId, Pageable pageable);
}
