package tech.impulso.evaluations.entity;

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
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Intento realizado por un estudiante sobre una evaluación
 * (RF-015 / RF-043).
 *
 * <p>El intento se crea al iniciar la evaluación y se finaliza al enviar
 * las respuestas. Cuando existe tiempo límite, el servicio calcula si
 * el intento se envió dentro del plazo utilizando {@link #startedAt}.</p>
 */
@Entity
@Table(name = "evaluation_attempts")
public class EvaluationAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Evaluación sobre la que se realiza el intento. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    /** Estudiante que realiza el intento. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Momento en que el estudiante comenzó el intento. */
    @Column(name = "started_at", nullable = false, updatable = false)
    private OffsetDateTime startedAt;

    /** Momento en que se envió el intento. Nulo mientras está en curso. */
    @Column(name = "finished_at")
    private OffsetDateTime finishedAt;

    /** Puntaje total obtenido en el intento. */
    @Column(name = "total_score", nullable = false)
    private int totalScore;

    /** Puntaje máximo posible en el intento (suma de las preguntas). */
    @Column(name = "max_possible_score", nullable = false)
    private int maxPossibleScore;

    /** Porcentaje de acierto obtenido (0 a 100). */
    @Column(name = "percentage", nullable = false)
    private int percentage;

    /** Indica si el intento alcanzó el porcentaje mínimo de aprobación. */
    @Column(name = "passed", nullable = false)
    private boolean passed;

    /** Respuestas enviadas por el estudiante, en JSON. */
    @Column(name = "answers", columnDefinition = "text")
    private String answers;

    @PrePersist
    void onCreate() {
        if (this.startedAt == null) {
            this.startedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OffsetDateTime getStartedAt() {
        return startedAt;
    }

    public OffsetDateTime getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(OffsetDateTime finishedAt) {
        this.finishedAt = finishedAt;
    }

    public int getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(int totalScore) {
        this.totalScore = totalScore;
    }

    public int getMaxPossibleScore() {
        return maxPossibleScore;
    }

    public void setMaxPossibleScore(int maxPossibleScore) {
        this.maxPossibleScore = maxPossibleScore;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getAnswers() {
        return answers;
    }

    public void setAnswers(String answers) {
        this.answers = answers;
    }
}
