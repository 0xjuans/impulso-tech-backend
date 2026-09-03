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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Conversación entre un usuario y la mascota IA (RF-017).
 *
 * <p>Cada conversación pertenece a un único usuario y puede vincularse a
 * un elemento de aprendizaje ({@link AiContextType}). El backend es la
 * autoridad sobre el contenido: las instrucciones del sistema jamás se
 * modifican con datos del usuario (RF-029).</p>
 */
@Entity
@Table(name = "ai_conversations")
public class AiConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario propietario de la conversación. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Título visible resumido de la conversación. */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * Tipo de contexto de aprendizaje asociado. Es nulo o
     * {@link AiContextType#GENERAL} para conversaciones sin referencia.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "context_type", length = 30)
    private AiContextType contextType;

    /**
     * Identificador de la entidad de contexto (lección, curso, etc.).
     * Es nulo cuando {@code contextType} es {@code GENERAL} o nulo.
     */
    @Column(name = "context_id")
    private Long contextId;

    /** Fecha de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Fecha de la última interacción. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Fecha de cierre de la conversación. Una conversación cerrada no
     * admite nuevos mensajes.
     */
    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public AiContextType getContextType() {
        return contextType;
    }

    public void setContextType(AiContextType contextType) {
        this.contextType = contextType;
    }

    public Long getContextId() {
        return contextId;
    }

    public void setContextId(Long contextId) {
        this.contextId = contextId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }
}
