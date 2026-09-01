package tech.impulso.courses.entity;

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
import tech.impulso.learningroutes.entity.LearningRoute;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Entidad que representa un curso de Impulso Tech (RF-040).
 *
 * <p>Cada curso pertenece a un instructor responsable y puede formar
 * parte de una ruta de aprendizaje. Los estudiantes pueden acceder a un
 * curso únicamente cuando su estado es {@link ContentStatus#PUBLICADO}.</p>
 */
@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre visible del curso. */
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    /** Descripción general del contenido del curso. */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    /** Objetivo de aprendizaje resumido. */
    @Column(name = "objective", columnDefinition = "text")
    private String objective;

    /** URL pública de la imagen o recurso visual representativo. */
    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    /** Nivel de dificultad del curso. */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private DifficultyLevel difficulty;

    /** Duración estimada del curso en horas. */
    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    /** Lenguaje o tecnología principal abordada por el curso. */
    @Column(name = "technology", length = 120)
    private String technology;

    /**
     * Ruta de aprendizaje a la que pertenece el curso. Es nulo cuando el
     * curso existe de manera independiente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_route_id")
    private LearningRoute learningRoute;

    /** Instructor responsable del curso. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    /** Estado del ciclo de vida del contenido. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContentStatus status;

    /**
     * Indica si al completar el curso se debe generar un certificado
     * digital verificable (RF-040).
     */
    @Column(name = "generates_certificate", nullable = false)
    private boolean generatesCertificate;

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

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public LearningRoute getLearningRoute() {
        return learningRoute;
    }

    public void setLearningRoute(LearningRoute learningRoute) {
        this.learningRoute = learningRoute;
    }

    public User getInstructor() {
        return instructor;
    }

    public void setInstructor(User instructor) {
        this.instructor = instructor;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
    }

    public boolean isGeneratesCertificate() {
        return generatesCertificate;
    }

    public void setGeneratesCertificate(boolean generatesCertificate) {
        this.generatesCertificate = generatesCertificate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
