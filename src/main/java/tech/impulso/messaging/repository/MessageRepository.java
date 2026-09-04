package tech.impulso.messaging.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.messaging.entity.Message;

import java.time.OffsetDateTime;
import java.util.Optional;

/**
 * Repositorio de persistencia para {@link Message}.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Devuelve los mensajes de una conversación paginados y ordenados
     * del más reciente al más antiguo.
     */
    Page<Message> findByConversationIdOrderBySentAtDesc(Long conversationId, Pageable pageable);

    /**
     * Devuelve el mensaje más reciente de la conversación indicada.
     * Se utiliza para construir la vista compacta de la lista de
     * conversaciones.
     */
    Optional<Message> findFirstByConversationIdOrderBySentAtDesc(Long conversationId);

    /**
     * Cuenta los mensajes no leídos que el usuario indicado tiene en la
     * conversación (es decir, mensajes cuyo emisor no es él y que aún
     * no fueron marcados como leídos).
     */
    @Query("""
            select count(m) from Message m
            where m.conversation.id = :conversationId
              and m.sender.id <> :userId
              and m.readAt is null
            """)
    long countUnreadFor(@Param("conversationId") Long conversationId,
                        @Param("userId") Long userId);

    /**
     * Marca como leídos todos los mensajes de la conversación cuyo
     * emisor no sea el usuario indicado. Se ejecuta cuando el usuario
     * abre la conversación.
     */
    @Modifying
    @Query("""
            update Message m
               set m.readAt = :now
             where m.conversation.id = :conversationId
               and m.sender.id <> :userId
               and m.readAt is null
            """)
    int markConversationAsRead(@Param("conversationId") Long conversationId,
                               @Param("userId") Long userId,
                               @Param("now") OffsetDateTime now);

    /**
     * Cuenta los mensajes enviados por el usuario en la ventana
     * indicada. Se utiliza para aplicar el límite anti-abuso (RF-034).
     */
    long countBySenderIdAndSentAtAfter(Long senderId, OffsetDateTime after);
}
