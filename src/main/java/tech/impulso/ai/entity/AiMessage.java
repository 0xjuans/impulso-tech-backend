package tech.impulso.ai.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Mensaje persistido dentro de una conversación con la mascota IA
 * (RF-017).
 *
 * <p>Se conserva el contenido literal para poder auditar la
 * conversación y para poder reconstruir el historial completo que se
 * envía al proveedor de IA en cada nueva interacción.</p>
 */
@Entity
@Table(name = "ai_messages")
public class AiMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Conversación a la que pertenece el mensaje. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private AiConversation conversation;

    /** Rol del emisor del mensaje. */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private AiMessageRole role;

    /** Contenido literal del mensaje. */
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    /**
     * Cantidad estimada de tokens consumidos por este mensaje. Se llena
     * cuando el proveedor de IA reporta la cifra; es nulo cuando no está
     * disponible.
     */
    @Column(name = "tokens_used")
    private Integer tokensUsed;

    /** Fecha de creación del mensaje. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public AiConversation getConversation() {
        return conversation;
    }

    public void setConversation(AiConversation conversation) {
        this.conversation = conversation;
    }

    public AiMessageRole getRole() {
        return role;
    }

    public void setRole(AiMessageRole role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTokensUsed() {
        return tokensUsed;
    }

    public void setTokensUsed(Integer tokensUsed) {
        this.tokensUsed = tokensUsed;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
