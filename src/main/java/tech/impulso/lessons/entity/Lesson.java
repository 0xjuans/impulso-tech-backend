package tech.impulso.lessons.entity;

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
import tech.impulso.courses.entity.CourseModule;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Entidad que representa una lección de un curso (RF-041).
 *
 * <p>Cada lección pertenece a un {@link CourseModule} y contiene el
 * material educativo que el estudiante debe consumir. El contenido se
 * almacena como texto (Markdown o HTML) para simplificar el modelo
 * inicial; los recursos multimedia complejos se gestionarán mediante
 * entidades independientes en versiones posteriores.</p>
 */
@Entity
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Módulo al que pertenece la lección. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    /** Título visible de la lección. */
    @Column(name = "title", nullable = false, length = 180)
    private String title;

    /** Descripción breve. */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** Objetivo de aprendizaje. */
    @Column(name = "objective", columnDefinition = "text")
    private String objective;

    /** Contenido educativo en Markdown o HTML. */
    @Column(name = "content", columnDefinition = "text")
    private String content;

    /** Duración estimada de la lección en minutos. */
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    /** Posición de la lección dentro del módulo, comenzando en 1. */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /** Indica si la lección es opcional para completar el módulo. */
    @Column(name = "is_optional", nullable = false)
    private boolean optional;

    /** Estado del ciclo de vida de la lección. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContentStatus status;

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

    public CourseModule getModule() {
        return module;
    }

    public void setModule(CourseModule module) {
        this.module = module;
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

    public String getObjective() {
        return objective;
    }

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getEstimatedDurationMinutes() {
        return estimatedDurationMinutes;
    }

    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) {
        this.estimatedDurationMinutes = estimatedDurationMinutes;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
