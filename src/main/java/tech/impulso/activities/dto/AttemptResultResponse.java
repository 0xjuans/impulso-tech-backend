package tech.impulso.activities.dto;

import tech.impulso.activities.entity.ActivityAttempt;

import java.time.OffsetDateTime;

/**
 * Resultado devuelto tras evaluar un intento del estudiante.
 *
 * @param attemptId       identificador del intento persistido.
 * @param correct         indica si la respuesta fue correcta.
 * @param score           puntaje obtenido.
 * @param maxScore        puntaje máximo de la actividad.
 * @param attemptsUsed    cantidad de intentos realizados por el usuario
 *                        (incluyendo el actual).
 * @param attemptsLeft    intentos restantes; es {@code null} cuando la
 *                        actividad admite intentos ilimitados.
 * @param xpAwarded       XP otorgada por este intento (mayor que cero
 *                        únicamente en el primer acierto).
 * @param createdAt       fecha y hora del intento.
 */
public record AttemptResultResponse(
        Long attemptId,
        boolean correct,
        int score,
        int maxScore,
        long attemptsUsed,
        Integer attemptsLeft,
        int xpAwarded,
        OffsetDateTime createdAt
) {

    /**
     * Construye la respuesta a partir del intento persistido y de la
     * información de contexto calculada por el servicio.
     */
    public static AttemptResultResponse from(ActivityAttempt attempt,
                                             int maxScore,
                                             long attemptsUsed,
                                             Integer attemptsLeft,
                                             int xpAwarded) {
        return new AttemptResultResponse(
                attempt.getId(),
                attempt.isCorrect(),
                attempt.getScore(),
                maxScore,
                attemptsUsed,
                attemptsLeft,
                xpAwarded,
                attempt.getCreatedAt()
        );
    }
}
