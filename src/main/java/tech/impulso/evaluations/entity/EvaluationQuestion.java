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
import jakarta.persistence.Table;
import tech.impulso.activities.entity.ActivityType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Pregunta que compone una evaluación (RF-015 / RF-043).
 *
 * <p>El tipo determina cómo se interpreta la configuración
 * ({@link #config}) y la respuesta enviada por el estudiante. Se
 * reutilizan los mismos tipos definidos para las actividades
 * ({@link ActivityType}) para conservar consistencia en la evaluación
 * automática.</p>
 */
@Entity
@Table(name = "evaluation_questions")
public class EvaluationQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Evaluación a la que pertenece la pregunta. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "evaluation_id", nullable = false)
    private Evaluation evaluation;

    /** Posición dentro de la evaluación. */
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    /** Tipo de pregunta. */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private ActivityType type;

    /** Enunciado de la pregunta. */
    @Column(name = "question_text", nullable = false, columnDefinition = "text")
    private String questionText;

    /** Puntaje asignado a esta pregunta. */
    @Column(name = "score", nullable = false)
    private int score;

    /** Configuración específica del tipo, en JSON (incluye la respuesta correcta). */
    @Column(name = "config", nullable = false, columnDefinition = "text")
    private String config;

    /** Fecha de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
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

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
