package tech.impulso.activities.entity;

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
 * Intento realizado por un estudiante sobre una actividad concreta
 * (RF-042).
 *
 * <p>Cada intento guarda la respuesta enviada (en formato JSON), si fue
 * correcta y el puntaje obtenido. El historial completo se conserva para
 * poder mostrar retroalimentación y estadísticas.</p>
 */
@Entity
@Table(name = "activity_attempts")
public class ActivityAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Actividad sobre la que se realizó el intento. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    /** Estudiante que realizó el intento. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Respuesta enviada por el estudiante en formato JSON. */
    @Column(name = "answer", nullable = false, columnDefinition = "text")
    private String answer;

    /** Indica si la respuesta fue evaluada como correcta. */
    @Column(name = "correct", nullable = false)
    private boolean correct;

    /** Puntaje obtenido en el intento. */
    @Column(name = "score", nullable = false)
    private int score;

    /** Fecha y hora del intento. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
