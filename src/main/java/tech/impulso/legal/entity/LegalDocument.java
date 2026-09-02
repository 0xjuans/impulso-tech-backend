package tech.impulso.legal.entity;

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
import jakarta.persistence.Table;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Versión publicada de un documento legal (RF-062).
 *
 * <p>Las versiones activas son aquellas cuya {@link #retiredAt} sigue
 * nula. Un tipo de documento puede tener sólo una versión activa a la
 * vez; al publicar una nueva se retira automáticamente la anterior.</p>
 */
@Entity
@Table(name = "legal_documents")
public class LegalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tipo del documento legal. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private LegalDocumentType type;

    /** Identificador de versión (por ejemplo, "1.0" o "2025-09-01"). */
    @Column(name = "version", nullable = false, length = 30)
    private String version;

    /** Título visible del documento. */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Contenido completo del documento (Markdown o HTML). */
    @Column(name = "content", nullable = false, columnDefinition = "text")
    private String content;

    /**
     * Indica si esta publicación exige que el usuario acepte esta
     * versión para seguir utilizando la plataforma.
     */
    @Column(name = "requires_acceptance", nullable = false)
    private boolean requiresAcceptance;

    /** Momento en que se publicó la versión. */
    @Column(name = "published_at", nullable = false)
    private OffsetDateTime publishedAt;

    /**
     * Momento en que la versión dejó de ser vigente. Es nula mientras
     * la versión sigue considerándose actual.
     */
    @Column(name = "retired_at")
    private OffsetDateTime retiredAt;

    /** Administrador que publicó la versión. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    /** Fecha de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        if (this.publishedAt == null) {
            this.publishedAt = now;
        }
    }

    public Long getId() {
        return id;
    }

    public LegalDocumentType getType() {
        return type;
    }

    public void setType(LegalDocumentType type) {
        this.type = type;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRequiresAcceptance() {
        return requiresAcceptance;
    }

    public void setRequiresAcceptance(boolean requiresAcceptance) {
        this.requiresAcceptance = requiresAcceptance;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public OffsetDateTime getRetiredAt() {
        return retiredAt;
    }

    public void setRetiredAt(OffsetDateTime retiredAt) {
        this.retiredAt = retiredAt;
    }

    public User getPublishedBy() {
        return publishedBy;
    }

    public void setPublishedBy(User publishedBy) {
        this.publishedBy = publishedBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
