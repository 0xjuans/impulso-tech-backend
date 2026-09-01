package tech.impulso.resources.entity;

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
import tech.impulso.courses.entity.Course;
import tech.impulso.courses.entity.CourseModule;
import tech.impulso.learningroutes.entity.LearningRoute;
import tech.impulso.lessons.entity.Lesson;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Recurso educativo disponible en la biblioteca de Impulso Tech
 * (RF-024).
 *
 * <p>Cada recurso puede vincularse opcionalmente a una ruta, curso,
 * módulo o lección. Todos los vínculos son opcionales para admitir
 * recursos generales que no dependan de un contenido específico.</p>
 */
@Entity
@Table(name = "educational_resources")
public class EducationalResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre visible del recurso. */
    @Column(name = "name", nullable = false, length = 180)
    private String name;

    /** Descripción general del recurso. */
    @Column(name = "description", columnDefinition = "text")
    private String description;

    /** Tipo funcional del recurso. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ResourceType type;

    /** Categoría temática libre configurada por el instructor. */
    @Column(name = "category", length = 120)
    private String category;

    /** Tema o subtema al que pertenece el recurso. */
    @Column(name = "topic", length = 150)
    private String topic;

    /** Tecnología o lenguaje relacionado, cuando aplique. */
    @Column(name = "technology", length = 120)
    private String technology;

    /** Nivel de dificultad sugerido para el recurso, cuando aplique. */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", length = 20)
    private DifficultyLevel difficulty;

    /**
     * Autor del contenido; puede ser una persona externa distinta al
     * usuario que registra el recurso en la plataforma.
     */
    @Column(name = "author", length = 150)
    private String author;

    /** URL del recurso: enlace externo o URL público del objeto en R2. */
    @Column(name = "resource_url", nullable = false, length = 1000)
    private String resourceUrl;

    /** Estado del ciclo de vida del contenido. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContentStatus status;

    /** Fecha en que el recurso fue publicado por primera vez. */
    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    /** Usuario que registró el recurso en la plataforma. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private User createdBy;

    /** Ruta de aprendizaje asociada, si aplica. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "learning_route_id")
    private LearningRoute learningRoute;

    /** Curso asociado, si aplica. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    /** Módulo asociado, si aplica. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private CourseModule module;

    /** Lección asociada, si aplica. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

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

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getResourceUrl() {
        return resourceUrl;
    }

    public void setResourceUrl(String resourceUrl) {
        this.resourceUrl = resourceUrl;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(OffsetDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LearningRoute getLearningRoute() {
        return learningRoute;
    }

    public void setLearningRoute(LearningRoute learningRoute) {
        this.learningRoute = learningRoute;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public CourseModule getModule() {
        return module;
    }

    public void setModule(CourseModule module) {
        this.module = module;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
