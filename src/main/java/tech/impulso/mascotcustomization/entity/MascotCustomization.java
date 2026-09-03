package tech.impulso.mascotcustomization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Personalización activa de la mascota de un usuario (RF-052).
 *
 * <p>Comparte el identificador con el usuario y guarda el ítem
 * equipado en cada ranura. Un valor nulo indica que la ranura utiliza
 * el aspecto por defecto.</p>
 */
@Entity
@Table(name = "mascot_customization")
public class MascotCustomization {

    /** Comparte el identificador con la tabla {@code users}. */
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skin_item_id")
    private MascotItem skin;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hat_item_id")
    private MascotItem hat;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accessory_item_id")
    private MascotItem accessory;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "background_item_id")
    private MascotItem background;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public MascotItem getSkin() { return skin; }
    public void setSkin(MascotItem skin) { this.skin = skin; }
    public MascotItem getHat() { return hat; }
    public void setHat(MascotItem hat) { this.hat = hat; }
    public MascotItem getAccessory() { return accessory; }
    public void setAccessory(MascotItem accessory) { this.accessory = accessory; }
    public MascotItem getBackground() { return background; }
    public void setBackground(MascotItem background) { this.background = background; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
