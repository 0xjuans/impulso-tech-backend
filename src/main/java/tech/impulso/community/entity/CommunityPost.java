package tech.impulso.community.entity;

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
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Publicación de la comunidad y foro de aprendizaje (RF-034).
 *
 * <p>Cada publicación pertenece a un autor y puede vincularse
 * opcionalmente a un contenido educativo mediante {@link #relatedType} y
 * {@link #relatedId} para facilitar el contexto de la consulta.</p>
 */
@Entity
@Table(name = "community_posts")
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Autor de la publicación. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** Título breve de la publicación. */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /** Descripción o pregunta desarrollada. */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    /** Fragmento de código opcional adjunto a la publicación. */
    @Column(name = "code_snippet", columnDefinition = "text")
    private String codeSnippet;

    /** Etiquetas asociadas, almacenadas como CSV. */
    @Column(name = "tags", length = 500)
    private String tags;

    /** Tipo del contenido educativo relacionado, si aplica. */
    @Enumerated(EnumType.STRING)
    @Column(name = "related_type", length = 30)
    private RelatedContentType relatedType;

    /** Identificador del contenido educativo relacionado. */
    @Column(name = "related_id")
    private Long relatedId;

    /**
     * Identificador de la respuesta marcada como aceptada, si el autor
     * de la publicación seleccionó alguna. Se almacena como identificador
     * suelto (sin relación JPA) para simplificar el manejo del ciclo de
     * vida entre publicación y respuestas.
     */
    @Column(name = "accepted_reply_id")
    private Long acceptedReplyId;

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

    public Long getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public RelatedContentType getRelatedType() {
        return relatedType;
    }

    public void setRelatedType(RelatedContentType relatedType) {
        this.relatedType = relatedType;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public Long getAcceptedReplyId() {
        return acceptedReplyId;
    }

    public void setAcceptedReplyId(Long acceptedReplyId) {
        this.acceptedReplyId = acceptedReplyId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
