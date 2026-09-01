package tech.impulso.gamification.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Insignia otorgable a los estudiantes al cumplir determinadas
 * condiciones (RF-019 / RF-046).
 */
@Entity
@Table(name = "badges")
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Código estable de la insignia. Se conserva aunque cambie el
     * nombre visible, permitiendo referenciarla desde el código sin
     * depender de identificadores generados.
     */
    @Column(name = "code", nullable = false, unique = true, length = 60)
    private String code;

    /** Nombre visible de la insignia. */
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    /** Descripción orientada al estudiante. */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    /** URL pública de la imagen o ícono representativo. */
    @Column(name = "icon_url", length = 500)
    private String iconUrl;

    /** Categoría temática. */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 30)
    private BadgeCategory category;

    /** Rareza visual utilizada para diferenciar los logros. */
    @Enumerated(EnumType.STRING)
    @Column(name = "rarity", nullable = false, length = 20)
    private BadgeRarity rarity;

    /** Condición automática que dispara el otorgamiento. */
    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false, length = 40)
    private BadgeTrigger triggerType;

    /** Umbral asociado al disparador. */
    @Column(name = "trigger_value", nullable = false)
    private int triggerValue;

    /** Indica si la insignia está disponible para nuevos otorgamientos. */
    @Column(name = "active", nullable = false)
    private boolean active;

    /** Fecha de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public BadgeCategory getCategory() {
        return category;
    }

    public void setCategory(BadgeCategory category) {
        this.category = category;
    }

    public BadgeRarity getRarity() {
        return rarity;
    }

    public void setRarity(BadgeRarity rarity) {
        this.rarity = rarity;
    }

    public BadgeTrigger getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(BadgeTrigger triggerType) {
        this.triggerType = triggerType;
    }

    public int getTriggerValue() {
        return triggerValue;
    }

    public void setTriggerValue(int triggerValue) {
        this.triggerValue = triggerValue;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
