package tech.impulso.messaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Mensaje enviado dentro de una conversación directa (RF-061).
 *
 * <p>Se persiste el contenido literal para permitir auditar la
 * comunicación. El campo {@code readAt} deja constancia del momento en
 * el que el receptor consultó el mensaje.</p>
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Conversación a la que pertenece el mensaje. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private MessageConversation conversation;

    /** Emisor del mensaje. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** Contenido literal enviado por el emisor. */
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    /** Fecha en que se envió el mensaje. */
    @Column(name = "sent_at", nullable = false, updatable = false)
    private OffsetDateTime sentAt;

    /** Fecha en la que el receptor marcó el mensaje como leído. */
    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @PrePersist
    void onCreate() {
        this.sentAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public MessageConversation getConversation() { return conversation; }
    public void setConversation(MessageConversation conversation) { this.conversation = conversation; }
    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public OffsetDateTime getSentAt() { return sentAt; }
    public OffsetDateTime getReadAt() { return readAt; }
    public void setReadAt(OffsetDateTime readAt) { this.readAt = readAt; }
}
