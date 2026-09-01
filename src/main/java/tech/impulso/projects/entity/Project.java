package tech.impulso.projects.entity;

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
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Plantilla de proyecto de programación (RF-023 / RF-045).
 *
 * <p>Cada proyecto pertenece a un instructor responsable y puede
 * asociarse opcionalmente a una ruta, curso o módulo. Los estudiantes
 * envían sus entregas mediante {@link ProjectSubmission}.</p>
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre visible del proyecto. */
    @Column(name = "name", nullable = false, length = 180)
    private String name;

    /** Descripción general del proyecto. */
    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;

    /** Objetivo de aprendizaje del proyecto. */
    @Column(name = "objective", columnDefinition = "text")
    private String objective;

    /** Instrucciones detalladas para el estudiante. */
    @Column(name = "instructions", columnDefinition = "text")
    private String instructions;

    /** Requisitos funcionales que debe cumplir la solución. */
    @Column(name = "requirements", columnDefinition = "text")
    private String requirements;

    /** Nivel de dificultad del proyecto. */
    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty", nullable = false, length = 20)
    private DifficultyLevel difficulty;

    /**
     * Lista de tecnologías o lenguajes esperados (texto separado por
     * comas, como en las rutas).
     */
    @Column(name = "technologies", length = 300)
    private String technologies;

    /** Recursos de apoyo sugeridos por el instructor. */
    @Column(name = "resources", columnDefinition = "text")
    private String resources;

    /** Criterios utilizados al evaluar la entrega. */
    @Column(name = "evaluation_criteria", columnDefinition = "text")
    private String evaluationCriteria;

    /** Puntaje máximo obtenible en la calificación (0 a 100). */
    @Column(name = "max_score", nullable = false)
    private int maxScore;

    /** XP otorgada la primera vez que se aprueba una entrega. */
    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    /** Fecha límite opcional para entregar el proyecto. */
    @Column(name = "deadline_at")
    private OffsetDateTime deadlineAt;

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

    /** Instructor responsable del proyecto. */
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

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getRequirements() {
        return requirements;
    }

    public void setRequirements(String requirements) {
        this.requirements = requirements;
    }

    public DifficultyLevel getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(DifficultyLevel difficulty) {
        this.difficulty = difficulty;
    }

    public String getTechnologies() {
        return technologies;
    }

    public void setTechnologies(String technologies) {
        this.technologies = technologies;
    }

    public String getResources() {
        return resources;
    }

    public void setResources(String resources) {
        this.resources = resources;
    }

    public String getEvaluationCriteria() {
        return evaluationCriteria;
    }

    public void setEvaluationCriteria(String evaluationCriteria) {
        this.evaluationCriteria = evaluationCriteria;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public OffsetDateTime getDeadlineAt() {
        return deadlineAt;
    }

    public void setDeadlineAt(OffsetDateTime deadlineAt) {
        this.deadlineAt = deadlineAt;
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
