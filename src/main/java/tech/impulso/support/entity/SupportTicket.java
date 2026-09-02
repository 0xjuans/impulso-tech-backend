package tech.impulso.support.entity;

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
 * Ticket de soporte o reporte de problema (RF-036).
 *
 * <p>Cada ticket pertenece al usuario que lo creó y puede asignarse a
 * un instructor o administrador para su seguimiento. Cuando el tipo del
 * ticket corresponde a un contenido educativo específico, el campo
 * {@link #relatedId} identifica al recurso afectado.</p>
 */
@Entity
@Table(name = "support_tickets")
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que reportó el problema. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /** Tipo del ticket. Determina el recurso relacionado, si aplica. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private SupportTicketType type;

    /** Título breve del ticket. */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Descripción detallada del problema. */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    /**
     * Identificador del recurso relacionado con el problema, cuando el
     * tipo del ticket corresponde a un contenido concreto.
     */
    @Column(name = "related_id")
    private Long relatedId;

    /** Estado actual del ciclo de vida del ticket. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SupportTicketStatus status;

    /**
     * Responsable asignado del ticket (instructor o administrador). Es
     * nulo mientras el ticket no haya sido tomado por nadie.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    /** Notas de resolución registradas por el responsable. */
    @Column(name = "resolution_notes", columnDefinition = "text")
    private String resolutionNotes;

    /** Momento en que el ticket fue marcado como resuelto. */
    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    /** Fecha de creación del ticket. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Fecha de la última actualización. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = SupportTicketStatus.PENDIENTE;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public SupportTicketType getType() {
        return type;
    }

    public void setType(SupportTicketType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public SupportTicketStatus getStatus() {
        return status;
    }

    public void setStatus(SupportTicketStatus status) {
        this.status = status;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public OffsetDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(OffsetDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
