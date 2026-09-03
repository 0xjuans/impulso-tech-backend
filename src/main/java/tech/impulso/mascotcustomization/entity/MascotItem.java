package tech.impulso.mascotcustomization.entity;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import tech.impulso.gamification.entity.Badge;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Ítem visual desbloqueable de la mascota (RF-022, RF-050, RF-052).
 *
 * <p>Cada ítem pertenece a una única ranura ({@link MascotItemSlot})
 * y define el criterio por el cual se concede automáticamente al
 * estudiante ({@link MascotUnlockType}). El backend es la autoridad
 * sobre las condiciones y su cumplimiento.</p>
 */
@Entity
@Table(name = "mascot_items")
public class MascotItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Código único usado por el frontend y las importaciones. */
    @Column(name = "code", nullable = false, unique = true, length = 60)
    private String code;

    /** Nombre visible del ítem. */
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    /** Descripción breve mostrada en el catálogo. */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** Ranura en la que se equipa el ítem. */
    @Enumerated(EnumType.STRING)
    @Column(name = "slot", nullable = false, length = 30)
    private MascotItemSlot slot;

    /** URL pública o llave del recurso visual del ítem. */
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** Criterio por el cual se desbloquea el ítem. */
    @Enumerated(EnumType.STRING)
    @Column(name = "unlock_type", nullable = false, length = 30)
    private MascotUnlockType unlockType;

    /** Nivel mínimo requerido cuando {@code unlockType = LEVEL}. */
    @Column(name = "unlock_level")
    private Integer unlockLevel;

    /** Cantidad mínima de XP requerida cuando {@code unlockType = XP}. */
    @Column(name = "unlock_xp")
    private Integer unlockXp;

    /** Insignia requerida cuando {@code unlockType = BADGE}. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unlock_badge_id")
    private Badge unlockBadge;

    /**
     * Indica si el ítem se encuentra activo en el catálogo. Los ítems
     * inactivos permanecen desbloqueados para quien ya los obtuvo pero
     * no se muestran a nuevos estudiantes.
     */
    @Column(name = "active", nullable = false)
    private boolean active;

    /** Fecha de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Fecha de la última modificación. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public MascotItemSlot getSlot() { return slot; }
    public void setSlot(MascotItemSlot slot) { this.slot = slot; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public MascotUnlockType getUnlockType() { return unlockType; }
    public void setUnlockType(MascotUnlockType unlockType) { this.unlockType = unlockType; }
    public Integer getUnlockLevel() { return unlockLevel; }
    public void setUnlockLevel(Integer unlockLevel) { this.unlockLevel = unlockLevel; }
    public Integer getUnlockXp() { return unlockXp; }
    public void setUnlockXp(Integer unlockXp) { this.unlockXp = unlockXp; }
    public Badge getUnlockBadge() { return unlockBadge; }
    public void setUnlockBadge(Badge unlockBadge) { this.unlockBadge = unlockBadge; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
