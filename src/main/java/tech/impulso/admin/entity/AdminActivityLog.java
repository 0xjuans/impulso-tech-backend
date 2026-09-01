package tech.impulso.admin.entity;

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
 * Entrada del registro de actividad administrativa (RF-056).
 *
 * <p>Cada instancia representa una acción sensible ejecutada dentro de la
 * plataforma. Los registros son inmutables: una vez creados no deberían
 * modificarse; sólo se agregan nuevos para mantener la trazabilidad.</p>
 */
@Entity
@Table(name = "admin_activity_log")
public class AdminActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Administrador responsable de la acción. Es nulo cuando la acción es
     * ejecutada por un proceso automático del sistema (por ejemplo, la
     * creación del primer administrador durante el arranque).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    /** Código corto que identifica la acción realizada. */
    @Column(name = "action", nullable = false, length = 80)
    private String action;

    /** Tipo del recurso afectado (por ejemplo, {@code USER}). */
    @Column(name = "target_type", length = 60)
    private String targetType;

    /** Identificador interno del recurso afectado. */
    @Column(name = "target_id")
    private Long targetId;

    /** Detalles adicionales de la acción, en texto libre o JSON. */
    @Column(name = "details", columnDefinition = "text")
    private String details;

    /** Fecha y hora en que se registró la acción. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public User getAdmin() {
        return admin;
    }

    public void setAdmin(User admin) {
        this.admin = admin;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Long getTargetId() {
        return targetId;
    }

    public void setTargetId(Long targetId) {
        this.targetId = targetId;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
