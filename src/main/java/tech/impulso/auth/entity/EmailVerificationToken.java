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
 * Token de un solo uso enviado al correo del usuario para confirmar la
 * propiedad de su cuenta durante el proceso de registro (RF-001).
 *
 * <p>Cada token tiene una fecha de expiración y una fecha de consumo. Una
 * vez consumido no puede volver a utilizarse; si expira antes de ser
 * usado, el usuario podrá solicitar uno nuevo.</p>
 */
@Entity
@Table(name = "email_verification_tokens")
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario propietario del token. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Valor aleatorio del token que se envía al correo del usuario. */
    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    /** Momento a partir del cual el token deja de ser válido. */
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    /**
     * Momento en que el token fue utilizado exitosamente. Es nulo mientras
     * el token no haya sido consumido.
     */
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
     * Indica si el token puede utilizarse en este momento.
     *
     * @return {@code true} cuando el token no ha sido consumido y aún no
     *         ha expirado.
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
