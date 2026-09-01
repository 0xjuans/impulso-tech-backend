package tech.impulso.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Estado agregado de gamificación de un usuario: total de experiencia
 * acumulada y nivel actual (RF-018 / RF-047).
 *
 * <p>Se mantiene una fila por usuario para poder consultar
 * eficientemente el XP y el nivel desde el perfil y los rankings sin
 * necesidad de recalcular a partir del historial en cada consulta.</p>
 */
@Entity
@Table(name = "user_xp")
public class UserXp {

    /** Identificador del usuario propietario del registro. */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** Usuario asociado; comparte identificador con la tabla users. */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    /** Total de experiencia acumulada por el usuario. */
    @Column(name = "total_xp", nullable = false)
    private int totalXp;

    /** Nivel actual calculado a partir de la XP total. */
    @Column(name = "current_level", nullable = false)
    private int currentLevel;

    /** Fecha de la última actualización del agregado. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
        if (this.currentLevel < 1) {
            this.currentLevel = 1;
        }
    }

    public Long getUserId() {
        return userId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
