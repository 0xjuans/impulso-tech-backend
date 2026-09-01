package tech.impulso.auth.entity;

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
 * Token de un solo uso enviado al correo del usuario para restablecer su
 * contraseña (RF-003 / RF-054).
 *
 * <p>El token tiene un tiempo de expiración limitado y no puede ser
 * reutilizado una vez consumido. Si expira sin ser utilizado, el usuario
 * podrá iniciar nuevamente el flujo de recuperación.</p>
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario propietario del token. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Valor aleatorio del token enviado al correo del usuario. */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /** Momento en que el token pierde su validez. */
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    /** Momento en que el token fue utilizado, si aplica. */
    @Column(name = "consumed_at")
    private OffsetDateTime consumedAt;

    /** Fecha de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    /**
     * Indica si el token está vigente y no ha sido consumido.
     *
     * @return {@code true} cuando el token es utilizable.
     */
    public boolean isUsable() {
        return consumedAt == null && expiresAt.isAfter(OffsetDateTime.now(ZoneOffset.UTC));
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public OffsetDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(OffsetDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public OffsetDateTime getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(OffsetDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
