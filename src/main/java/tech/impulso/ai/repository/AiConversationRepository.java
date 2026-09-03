package tech.impulso.ai.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.ai.entity.AiConversation;

/**
 * Repositorio de persistencia para {@link AiConversation}.
 */
@Repository
public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {

    /**
     * Devuelve las conversaciones de un usuario ordenadas de la más
     * reciente a la más antigua según el {@link Pageable}.
     */
    Page<AiConversation> findByUserId(Long userId, Pageable pageable);

    /**
     * Cuenta cuántas conversaciones ha creado el usuario en la ventana
     * temporal indicada. Se utiliza para aplicar el límite anti-abuso
     * (RF-034).
     */
    long countByUserIdAndCreatedAtAfter(Long userId, java.time.OffsetDateTime after);
}
