package tech.impulso.challenges.entity;

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
 * Reto de programación publicado por un instructor (RF-014).
 *
 * <p>Los casos de prueba se almacenan como texto libre; cuando la
 * plataforma incorpore la ejecución automática de código (RF-037), este
 * campo servirá como fuente para los ejecutores de pruebas.</p>
 */
@Entity
@Table(name = "challenges")
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre visible del reto. */
    @Column(name = "name", nullable = false, length = 180)
    private String name;

    /** Descripción general del reto. */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    /** Objetivo de aprendizaje. */
    @Column(name = "objective", columnDefinition = "text")
    private String objective;

    /** Instrucciones detalladas para el estudiante. */
    @Column(name = "instructions", columnDefinition = "text")
    private String instructions;

    /** Nivel de dificultad. */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private DifficultyLevel difficulty;

    /** Lenguajes permitidos, en formato CSV. */
    @Column(name = "allowed_languages", nullable = false, length = 300)
    private String allowedLanguages;

    /** Ejemplos de entrada y salida esperados. */
    @Column(name = "io_examples", columnDefinition = "text")
    private String ioExamples;

    /** Restricciones específicas del reto. */
    @Column(name = "restrictions", columnDefinition = "text")
    private String restrictions;

    /** Casos de prueba visibles al estudiante como referencia. */
    @Column(name = "public_test_cases", columnDefinition = "text")
    private String publicTestCases;

    /** Casos de prueba ocultos utilizados para calificación. */
    @Column(name = "hidden_test_cases", columnDefinition = "text")
    private String hiddenTestCases;

    /** XP otorgada la primera vez que se aprueba una solución. */
    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    /** Tiempo estimado de resolución en minutos. */
    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    /** Estado del ciclo de vida del contenido. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ContentStatus status;

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

    /** Instructor responsable del reto. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instructor_id", nullable = false)
    private User instructor;

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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public String getAllowedLanguages() {
        return allowedLanguages;
    }

    public void setAllowedLanguages(String allowedLanguages) {
        this.allowedLanguages = allowedLanguages;
    }

    public String getIoExamples() {
        return ioExamples;
    }

    public void setIoExamples(String ioExamples) {
        this.ioExamples = ioExamples;
    }

    public String getRestrictions() {
        return restrictions;
    }

    public void setRestrictions(String restrictions) {
        this.restrictions = restrictions;
    }

    public String getPublicTestCases() {
        return publicTestCases;
    }

    public void setPublicTestCases(String publicTestCases) {
        this.publicTestCases = publicTestCases;
    }

    public String getHiddenTestCases() {
        return hiddenTestCases;
    }

    public void setHiddenTestCases(String hiddenTestCases) {
        this.hiddenTestCases = hiddenTestCases;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }

    public ContentStatus getStatus() {
        return status;
    }

    public void setStatus(ContentStatus status) {
        this.status = status;
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
