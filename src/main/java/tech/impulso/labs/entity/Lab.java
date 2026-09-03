package tech.impulso.labs.entity;

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
import tech.impulso.courses.entity.Course;
import tech.impulso.lessons.entity.Lesson;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Laboratorio práctico de programación (RF-013).
 *
 * <p>Cada laboratorio expone unas instrucciones, un lenguaje objetivo y
 * opcionalmente un fragmento de código inicial y una salida esperada.
 * El estudiante escribe su propio código, lo ejecuta en el sandbox
 * aislado (RF-030) y puede enviarlo como entrega para dejar
 * constancia de su solución.</p>
 */
@Entity
@Table(name = "labs")
public class Lab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Título visible del laboratorio. */
    @Column(name = "title", nullable = false, length = 180)
    private String title;

    /** Descripción general del laboratorio. */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    /** Instrucciones detalladas para el estudiante. */
    @Column(name = "instructions", nullable = false, columnDefinition = "text")
    private String instructions;

    /**
     * Lenguaje de programación soportado, en formato normalizado
     * (por ejemplo: {@code python}, {@code java}, {@code javascript}).
     */
    @Column(name = "language", nullable = false, length = 60)
    private String language;

    /** Código inicial sugerido para el estudiante, opcional. */
    @Column(name = "starter_code", columnDefinition = "text")
    private String starterCode;

    /**
     * Salida esperada por {@code stdout} para considerar el laboratorio
     * completado. Es opcional: cuando es nulo el laboratorio es
     * exploratorio y no se autocalifica.
     */
    @Column(name = "expected_output", columnDefinition = "text")
    private String expectedOutput;

    /** Tiempo máximo de ejecución permitido en milisegundos. */
    @Column(name = "execution_timeout_ms", nullable = false)
    private int executionTimeoutMs;

    /** Curso al que pertenece el laboratorio; puede ser nulo. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /** Lección asociada al laboratorio; puede ser nula. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    /** Instructor responsable del laboratorio. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

    /** Estado del ciclo de vida del contenido. */
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
        if (this.executionTimeoutMs <= 0) {
            this.executionTimeoutMs = 5000;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public String getStarterCode() { return starterCode; }
    public void setStarterCode(String starterCode) { this.starterCode = starterCode; }
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    public int getExecutionTimeoutMs() { return executionTimeoutMs; }
    public void setExecutionTimeoutMs(int executionTimeoutMs) { this.executionTimeoutMs = executionTimeoutMs; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }
    public User getInstructor() { return instructor; }
    public void setInstructor(User instructor) { this.instructor = instructor; }
    public ContentStatus getStatus() { return status; }
    public void setStatus(ContentStatus status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
