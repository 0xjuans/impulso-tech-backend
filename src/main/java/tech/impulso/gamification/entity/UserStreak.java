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

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Racha de aprendizaje de un usuario (RF-020 / RF-048).
 *
 * <p>Registra la cantidad de días consecutivos con actividad válida,
 * el récord histórico y la fecha del último día que contó como
 * actividad. Sólo se considera actividad válida la realización efectiva
 * de contenido (por ejemplo, completar una lección); iniciar sesión no
 * mantiene la racha.</p>
 */
@Entity
@Table(name = "user_streaks")
public class UserStreak {

    /** Identificador del usuario propietario del registro. */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** Usuario asociado; comparte identificador con la tabla users. */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    /** Días consecutivos actuales con actividad válida. */
    @Column(name = "current_streak", nullable = false)
    private int currentStreak;

    /** Récord histórico de días consecutivos alcanzado por el usuario. */
    @Column(name = "longest_streak", nullable = false)
    private int longestStreak;

    /** Fecha (UTC) del último día que contó como actividad válida. */
    @Column(name = "last_activity_date")
    private LocalDate lastActivityDate;

    /** Fecha (UTC) en que comenzó la racha actual. */
    @Column(name = "streak_started_on")
    private LocalDate streakStartedOn;

    /** Fecha de la última actualización del agregado. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
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

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public void setLongestStreak(int longestStreak) {
        this.longestStreak = longestStreak;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    public LocalDate getStreakStartedOn() {
        return streakStartedOn;
    }

    public void setStreakStartedOn(LocalDate streakStartedOn) {
        this.streakStartedOn = streakStartedOn;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
