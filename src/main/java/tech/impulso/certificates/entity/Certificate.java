package tech.impulso.certificates.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import tech.impulso.courses.entity.Course;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Certificado de finalización de curso otorgado a un estudiante (RF-047).
 *
 * <p>La restricción única {@code (user_id, course_id)} garantiza la
 * idempotencia de la emisión: un mismo estudiante nunca puede recibir dos
 * certificados de un mismo curso.</p>
 */
@Entity
@Table(name = "certificates",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_certificates_user_course",
                        columnNames = {"user_id", "course_id"}),
                @UniqueConstraint(name = "uk_certificates_verification",
                        columnNames = {"verification_code"})
        })
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Estudiante al que se le emite el certificado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Curso completado. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    /**
     * Código público (UUID) usado para verificar el certificado sin
     * exponer identificadores internos.
     */
    @Column(name = "verification_code", nullable = false, unique = true, updatable = false)
    private UUID verificationCode;

    /** Fecha de emisión (UTC). */
    @Column(name = "issued_at", nullable = false, updatable = false)
    private OffsetDateTime issuedAt;

    @PrePersist
    void onCreate() {
        if (this.verificationCode == null) {
            this.verificationCode = UUID.randomUUID();
        }
        if (this.issuedAt == null) {
            this.issuedAt = OffsetDateTime.now(ZoneOffset.UTC);
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

    public UUID getVerificationCode() {
        return verificationCode;
    }

    public void setVerificationCode(UUID verificationCode) {
        this.verificationCode = verificationCode;
    }

    public OffsetDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(OffsetDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
