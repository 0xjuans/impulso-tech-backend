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
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Entrega de un proyecto realizada por un estudiante
 * (RF-023 / RF-045).
 *
 * <p>El estudiante puede enviar múltiples versiones (identificadas por
 * {@link #submissionNumber}) sobre el mismo proyecto, típicamente cuando
 * el instructor solicita correcciones. Cada entrega conserva su propio
 * estado, calificación y retroalimentación.</p>
 */
@Entity
@Table(name = "project_submissions")
public class ProjectSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Proyecto al que pertenece la entrega. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /** Estudiante que realizó la entrega. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Número secuencial de la entrega dentro del proyecto por parte del
     * mismo estudiante. Comienza en 1.
     */
    @Column(name = "submission_number", nullable = false)
    private Integer submissionNumber;

    /**
     * URL con el trabajo entregado (por ejemplo, repositorio Git,
     * documento compartido o archivo en R2).
     */
    @Column(name = "submission_url", nullable = false, length = 1000)
    private String submissionUrl;

    /** Notas o comentarios del estudiante sobre su entrega. */
    @Column(name = "student_notes", columnDefinition = "text")
    private String studentNotes;

    /** Estado del ciclo de vida de la entrega. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ProjectSubmissionStatus status;

    /** Calificación otorgada por el instructor (0 a 100). */
    @Column(name = "grade")
    private Integer grade;

    /** Retroalimentación textual del instructor. */
    @Column(name = "feedback", columnDefinition = "text")
    private String feedback;

    /**
     * Indica si la entrega se realizó después de la fecha límite del
     * proyecto.
     */
    @Column(name = "late_submission", nullable = false)
    private boolean lateSubmission;

    /** Instructor que revisó la entrega. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    /** Momento en que se registró la revisión. */
    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;

    /** Momento en que el estudiante envió la entrega. */
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    /** Fecha de la última modificación del registro. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (this.submittedAt == null) {
            this.submittedAt = now;
        }
        this.updatedAt = now;
        if (this.status == null) {
            this.status = ProjectSubmissionStatus.ENVIADA;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getSubmissionNumber() {
        return submissionNumber;
    }

    public void setSubmissionNumber(Integer submissionNumber) {
        this.submissionNumber = submissionNumber;
    }

    public String getSubmissionUrl() {
        return submissionUrl;
    }

    public void setSubmissionUrl(String submissionUrl) {
        this.submissionUrl = submissionUrl;
    }

    public String getStudentNotes() {
        return studentNotes;
    }

    public void setStudentNotes(String studentNotes) {
        this.studentNotes = studentNotes;
    }

    public ProjectSubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectSubmissionStatus status) {
        this.status = status;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public boolean isLateSubmission() {
        return lateSubmission;
    }

    public void setLateSubmission(boolean lateSubmission) {
        this.lateSubmission = lateSubmission;
    }

    public User getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(User reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public OffsetDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(OffsetDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public OffsetDateTime getSubmittedAt() {
        return submittedAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
