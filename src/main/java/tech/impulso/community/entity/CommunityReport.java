package tech.impulso.community.entity;

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
 * Reporte de contenido inapropiado en la comunidad (RF-034,
 * moderación).
 *
 * <p>La restricción {@code UNIQUE(reporter_id, target_type, target_id)}
 * en base de datos impide que un mismo usuario reporte el mismo
 * contenido más de una vez.</p>
 */
@Entity
@Table(name = "community_reports")
public class CommunityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que emitió el reporte. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    /** Tipo del contenido reportado. */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 10)
    private ReportTargetType targetType;

    /** Identificador del contenido reportado (post o respuesta). */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /** Motivo textual del reporte suministrado por el usuario. */
    @Column(name = "reason", nullable = false, columnDefinition = "text")
    private String reason;

    /** Estado actual del reporte. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status;

    /**
     * Administrador que revisó el reporte. Es nulo mientras el reporte
     * permanezca en estado {@link ReportStatus#PENDIENTE}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    /** Momento en que el administrador revisó el reporte. */
    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    /** Observaciones internas registradas por el administrador. */
    @Column(name = "admin_notes", columnDefinition = "text")
    private String adminNotes;

    /** Fecha de creación del reporte. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        if (this.status == null) {
            this.status = ReportStatus.PENDIENTE;
        }
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

    public ReportTargetType getTargetType() {
        return targetType;
    }

    public void setTargetType(ReportTargetType targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(OffsetDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
