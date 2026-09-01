package tech.impulso.learningroutes.entity;

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
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Entidad que representa una ruta de aprendizaje (RF-039).
 *
 * <p>Una ruta agrupa cursos organizados en un orden sugerido y define
 * un objetivo de aprendizaje concreto para el estudiante. La estructura
 * detallada (cursos, módulos, lecciones) se maneja mediante entidades
 * separadas.</p>
 */
@Entity
@Table(name = "learning_routes")
public class LearningRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre visible de la ruta. */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Descripción general del propósito y contenido de la ruta. */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    /** Objetivo de aprendizaje resumido. */
    @Column(name = "objective", columnDefinition = "text")
    private String objective;

    /** URL pública de la imagen o recurso visual representativo. */
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    /** Nivel de dificultad de la ruta. */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private DifficultyLevel difficulty;

    /** Duración estimada expresada en horas. */
    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    /**
     * Lista de tecnologías o lenguajes relacionados con la ruta,
     * almacenada como texto separado por comas para simplificar el
     * modelo inicial.
     */
    @Column(name = "technologies", length = 300)
    private String technologies;

    /** Estado del ciclo de vida del contenido. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContentStatus status;

    /** Instructor responsable de la ruta. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    /** Fecha de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Fecha de la última modificación del registro. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ContentStatus.BORRADOR;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
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

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getEstimatedDurationHours() {
        return estimatedDurationHours;
    }

    public void setEstimatedDurationHours(Integer estimatedDurationHours) {
        this.estimatedDurationHours = estimatedDurationHours;
    }

    public String getTechnologies() {
        return technologies;
    }

    public void setTechnologies(String technologies) {
        this.technologies = technologies;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
    }

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
