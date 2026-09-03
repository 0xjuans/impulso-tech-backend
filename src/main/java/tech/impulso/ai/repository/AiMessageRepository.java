package tech.impulso.ai.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.ai.entity.AiMessage;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Repositorio de persistencia para {@link AiMessage}.
 */
@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {

    /**
     * Devuelve todos los mensajes de una conversación ordenados
     * cronológicamente. Incluye los mensajes de sistema para permitir
     * reconstruir la conversación completa.
     */
    List<AiMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /**
     * Cuenta cuántos mensajes de usuario ha enviado un usuario dentro de
     * la ventana temporal indicada. Se utiliza para aplicar el límite
     * anti-abuso (RF-034).
     */
    @org.springframework.data.jpa.repository.Query("""
            select count(m) from AiMessage m
            where m.conversation.user.id = :userId
              and m.role = tech.impulso.ai.entity.AiMessageRole.USER
              and m.createdAt > :after
            """)
    long countUserMessagesAfter(
            @org.springframework.data.repository.query.Param("userId") Long userId,
            @org.springframework.data.repository.query.Param("after") OffsetDateTime after);
}
