package tech.impulso.mascotcustomization.entity;

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
import jakarta.persistence.UniqueConstraint;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Registro de un ítem desbloqueado por un usuario (RF-022, RF-050).
 *
 * <p>Se crea automáticamente cuando el estudiante cumple el requisito
 * asociado al ítem. La restricción única sobre {@code (user_id, item_id)}
 * evita duplicados.</p>
 */
@Entity
@Table(name = "mascot_unlocks",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mascot_unlocks_user_item",
                columnNames = {"user_id", "item_id"}))
public class MascotUnlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que desbloqueó el ítem. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Ítem desbloqueado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_id", nullable = false)
    private MascotItem item;

    /** Fecha en que se registró el desbloqueo. */
    @Column(name = "unlocked_at", nullable = false, updatable = false)
    private OffsetDateTime unlockedAt;

    @PrePersist
    void onCreate() {
        this.unlockedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public MascotItem getItem() { return item; }
    public void setItem(MascotItem item) { this.item = item; }
    public OffsetDateTime getUnlockedAt() { return unlockedAt; }
}
