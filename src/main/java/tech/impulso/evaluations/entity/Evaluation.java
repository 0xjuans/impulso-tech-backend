package tech.impulso.evaluations.entity;

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
import tech.impulso.lessons.entity.Lesson;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Evaluación asociada a una lección (RF-015 / RF-043).
 *
 * <p>Agrupa varias preguntas y define el tiempo disponible, el
 * porcentaje mínimo de aprobación y el número máximo de intentos
 * permitidos.</p>
 */
@Entity
@Table(name = "evaluations")
public class Evaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Lección a la que pertenece la evaluación. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    /** Nombre visible de la evaluación. */
    @Column(name = "name", nullable = false, length = 180)
    private String name;

    /** Descripción general. */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** Instrucciones detalladas para el estudiante. */
    @Column(name = "instructions", columnDefinition = "text")
    private String instructions;

    /**
     * Tiempo máximo permitido en minutos. Es {@code null} cuando la
     * evaluación no tiene límite de tiempo.
     */
    @Column(name = "time_limit_minutes")
    private Integer timeLimitMinutes;

    /** Porcentaje mínimo requerido para aprobar (0 a 100). */
    @Column(name = "passing_percentage", nullable = false)
    private int passingPercentage;

    /** Cantidad máxima de intentos permitidos. */
    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    /** Posición de la evaluación dentro de la lección. */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /** Estado del ciclo de vida de la evaluación. */
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

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Integer getTimeLimitMinutes() {
        return timeLimitMinutes;
    }

    public void setTimeLimitMinutes(Integer timeLimitMinutes) {
        this.timeLimitMinutes = timeLimitMinutes;
    }

    public int getPassingPercentage() {
        return passingPercentage;
    }

    public void setPassingPercentage(int passingPercentage) {
        this.passingPercentage = passingPercentage;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
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
