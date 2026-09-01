package tech.impulso.notifications.entity;

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
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Notificación dirigida a un usuario específico (RF-026).
 *
 * <p>Cada notificación describe un evento relevante ocurrido en la
 * plataforma y puede vincularse a un recurso concreto (curso, insignia,
 * evaluación, etc.) para permitir la navegación desde el centro de
 * notificaciones.</p>
 */
@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario destinatario. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Tipo funcional de la notificación. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private NotificationType type;

    /** Título breve de la notificación. */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Mensaje detallado orientado al usuario. */
    @Column(name = "message", nullable = false, columnDefinition = "text")
    private String message;

    /** Tipo del recurso relacionado, si aplica (por ejemplo, COURSE). */
    @Column(name = "related_type", length = 40)
    private String relatedType;

    /** Identificador del recurso relacionado, si aplica. */
    @Column(name = "related_id")
    private Long relatedId;

    /** Momento en que la notificación fue marcada como leída. */
    @Column(name = "read_at")
    private OffsetDateTime readAt;

    /** Fecha y hora en que se generó la notificación. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
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

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(String relatedType) {
        this.relatedType = relatedType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public OffsetDateTime getReadAt() {
        return readAt;
    }

    public void setReadAt(OffsetDateTime readAt) {
        this.readAt = readAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
