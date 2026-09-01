package tech.impulso.gamification.entity;

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
 * Registro individual del otorgamiento de experiencia (XP) a un
 * estudiante (RF-018).
 *
 * <p>Cada entrada indica la cantidad de XP otorgada, la fuente que la
 * originó y el identificador del recurso asociado. La restricción única
 * {@code (user_id, source_type, source_id)} garantiza la idempotencia:
 * un mismo evento no puede otorgar experiencia dos veces al mismo
 * usuario.</p>
 */
@Entity
@Table(name = "xp_events")
public class XpEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que recibe la experiencia. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Tipo de la fuente que originó el otorgamiento. */
    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 40)
    private XpSource sourceType;

    /** Identificador del recurso que originó el otorgamiento. */
    @Column(name = "source_id")
    private Long sourceId;

    /** Cantidad de XP otorgada por este evento. */
    @Column(name = "xp_awarded", nullable = false)
    private int xpAwarded;

    /** Fecha y hora del otorgamiento. */
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

    public XpSource getSourceType() {
        return sourceType;
    }

    public void setSourceType(XpSource sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public int getXpAwarded() {
        return xpAwarded;
    }

    public void setXpAwarded(int xpAwarded) {
        this.xpAwarded = xpAwarded;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
