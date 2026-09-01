package tech.impulso.resources.entity;

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
 * Marca de favorito de un recurso educativo por parte de un estudiante
 * (RF-025).
 *
 * <p>La restricción {@code UNIQUE(user_id, resource_id)} en base de
 * datos impide favoritos duplicados y complementa la validación previa
 * realizada por el servicio.</p>
 */
@Entity
@Table(name = "resource_favorites")
public class ResourceFavorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Estudiante que marcó el recurso como favorito. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Recurso marcado como favorito. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private EducationalResource resource;

    /** Momento en que se agregó a favoritos. */
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

    public EducationalResource getResource() {
        return resource;
    }

    public void setResource(EducationalResource resource) {
        this.resource = resource;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
