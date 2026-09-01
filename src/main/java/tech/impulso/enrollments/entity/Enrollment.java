package tech.impulso.enrollments.entity;

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
import jakarta.persistence.Table;
import tech.impulso.courses.entity.Course;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Inscripción de un estudiante a un curso (RF-011).
 *
 * <p>Representa el vínculo entre un usuario y un curso, junto con el
 * estado agregado del avance del estudiante y las marcas de tiempo del
 * inicio, último acceso y finalización.</p>
 */
@Entity
@Table(name = "enrollments")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Estudiante inscrito. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Curso en el que el estudiante se inscribió. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /** Estado agregado del avance del estudiante. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status;

    /** Fecha en que el estudiante inició el curso. */
    @Column(name = "started_at", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    /**
     * Fecha en que el estudiante completó el curso. Es nula mientras no
     * cumpla las condiciones de finalización.
     */
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    /**
     * Fecha del último acceso o avance registrado en el curso. Se
     * utiliza para ordenar por actividad y calcular estadísticas.
     */
    @Column(name = "last_accessed_at", nullable = false)
    private OffsetDateTime lastAccessedAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (this.startedAt == null) {
            this.startedAt = now;
        }
        if (this.lastAccessedAt == null) {
            this.lastAccessedAt = now;
        }
        if (this.status == null) {
            this.status = EnrollmentStatus.INSCRITO;
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public OffsetDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(OffsetDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
}
