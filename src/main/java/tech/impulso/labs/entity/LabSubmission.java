package tech.impulso.labs.entity;

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
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Entrega de un estudiante sobre un laboratorio (RF-013, RF-038).
 *
 * <p>Cada entrega registra el código enviado por el estudiante y el
 * resultado de su ejecución en el sandbox. Cuando el laboratorio define
 * una salida esperada, {@code passed} indica si la salida del programa
 * coincide con ella tras normalizar espacios finales.</p>
 */
@Entity
@Table(name = "lab_submissions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_lab_submissions_user_number",
                columnNames = {"lab_id", "user_id", "submission_number"}))
public class LabSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Laboratorio al que corresponde la entrega. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lab_id", nullable = false)
    private Lab lab;

    /** Estudiante que envió la solución. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Número consecutivo de la entrega dentro del par (laboratorio,
     * usuario). Comienza en 1.
     */
    @Column(name = "submission_number", nullable = false)
    private int submissionNumber;

    /** Código enviado por el estudiante. */
    @Column(name = "code", nullable = false, columnDefinition = "text")
    private String code;

    /** Salida por {@code stdout} capturada durante la ejecución. */
    @Column(name = "stdout", columnDefinition = "text")
    private String stdout;

    /** Salida por {@code stderr} capturada durante la ejecución. */
    @Column(name = "stderr", columnDefinition = "text")
    private String stderr;

    /** Código de salida del proceso; {@code null} si no se dispone. */
    @Column(name = "exit_code")
    private Integer exitCode;

    /** Duración total de la ejecución en milisegundos. */
    @Column(name = "execution_time_ms")
    private Integer executionTimeMs;

    /**
     * Indica si el resultado se considera aprobado. Es {@code false}
     * cuando la ejecución falla o cuando la salida no coincide con la
     * salida esperada del laboratorio.
     */
    @Column(name = "passed", nullable = false)
    private boolean passed;

    /** Fecha de la entrega. */
    @Column(name = "submitted_at", nullable = false, updatable = false)
    private OffsetDateTime submittedAt;

    @PrePersist
    void onCreate() {
        this.submittedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public Lab getLab() { return lab; }
    public void setLab(Lab lab) { this.lab = lab; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public int getSubmissionNumber() { return submissionNumber; }
    public void setSubmissionNumber(int submissionNumber) { this.submissionNumber = submissionNumber; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getStdout() { return stdout; }
    public void setStdout(String stdout) { this.stdout = stdout; }
    public String getStderr() { return stderr; }
    public void setStderr(String stderr) { this.stderr = stderr; }
    public Integer getExitCode() { return exitCode; }
    public void setExitCode(Integer exitCode) { this.exitCode = exitCode; }
    public Integer getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Integer executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public boolean isPassed() { return passed; }
    public void setPassed(boolean passed) { this.passed = passed; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
}
