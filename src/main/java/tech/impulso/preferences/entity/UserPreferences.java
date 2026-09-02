package tech.impulso.preferences.entity;

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
 * Preferencias personales del usuario (RF-060).
 *
 * <p>Se mantiene una fila por usuario; los valores por defecto se
 * aplican al crear la fila. Las preferencias controlan qué categorías
 * de notificaciones desea recibir el usuario, si acepta usar la mascota
 * virtual y otras opciones básicas de privacidad.</p>
 */
@Entity
@Table(name = "user_preferences")
public class UserPreferences {

    /** Identificador del usuario propietario. */
    @Id
    @Column(name = "user_id")
    private Long userId;

    /** Usuario asociado; comparte identificador con la tabla users. */
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    /** Recibe notificaciones sobre su progreso. */
    @Column(name = "notify_progress", nullable = false)
    private boolean notifyProgress = true;

    /** Recibe notificaciones relacionadas con retos. */
    @Column(name = "notify_challenges", nullable = false)
    private boolean notifyChallenges = true;

    /** Recibe notificaciones relacionadas con evaluaciones. */
    @Column(name = "notify_evaluations", nullable = false)
    private boolean notifyEvaluations = true;

    /** Recibe notificaciones de logros, insignias y recompensas. */
    @Column(name = "notify_achievements", nullable = false)
    private boolean notifyAchievements = true;

    /** Recibe recordatorios generados por la plataforma. */
    @Column(name = "notify_reminders", nullable = false)
    private boolean notifyReminders = true;

    /** Recibe los mensajes proactivos de la mascota virtual. */
    @Column(name = "notify_mascot", nullable = false)
    private boolean notifyMascot = true;

    /** Recibe también las notificaciones por correo electrónico. */
    @Column(name = "notify_by_email", nullable = false)
    private boolean notifyByEmail = false;

    /** Utiliza la mascota virtual con inteligencia artificial. */
    @Column(name = "ai_mascot_enabled", nullable = false)
    private boolean aiMascotEnabled = true;

    /** Su perfil se muestra públicamente. */
    @Column(name = "profile_public", nullable = false)
    private boolean profilePublic = true;

    /** Código ISO del idioma preferido para la interfaz. */
    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage = "es";

    /** Fecha de la última actualización del registro. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
        if (this.preferredLanguage == null || this.preferredLanguage.isBlank()) {
            this.preferredLanguage = "es";
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

    public boolean isNotifyProgress() {
        return notifyProgress;
    }

    public void setNotifyProgress(boolean notifyProgress) {
        this.notifyProgress = notifyProgress;
    }

    public boolean isNotifyChallenges() {
        return notifyChallenges;
    }

    public void setNotifyChallenges(boolean notifyChallenges) {
        this.notifyChallenges = notifyChallenges;
    }

    public boolean isNotifyEvaluations() {
        return notifyEvaluations;
    }

    public void setNotifyEvaluations(boolean notifyEvaluations) {
        this.notifyEvaluations = notifyEvaluations;
    }

    public boolean isNotifyAchievements() {
        return notifyAchievements;
    }

    public void setNotifyAchievements(boolean notifyAchievements) {
        this.notifyAchievements = notifyAchievements;
    }

    public boolean isNotifyReminders() {
        return notifyReminders;
    }

    public void setNotifyReminders(boolean notifyReminders) {
        this.notifyReminders = notifyReminders;
    }

    public boolean isNotifyMascot() {
        return notifyMascot;
    }

    public void setNotifyMascot(boolean notifyMascot) {
        this.notifyMascot = notifyMascot;
    }

    public boolean isNotifyByEmail() {
        return notifyByEmail;
    }

    public void setNotifyByEmail(boolean notifyByEmail) {
        this.notifyByEmail = notifyByEmail;
    }

    public boolean isAiMascotEnabled() {
        return aiMascotEnabled;
    }

    public void setAiMascotEnabled(boolean aiMascotEnabled) {
        this.aiMascotEnabled = aiMascotEnabled;
    }

    public boolean isProfilePublic() {
        return profilePublic;
    }

    public void setProfilePublic(boolean profilePublic) {
        this.profilePublic = profilePublic;
    }

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
