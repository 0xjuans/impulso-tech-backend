package tech.impulso.gamification.dto;

import tech.impulso.gamification.entity.UserStreak;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Representación pública de la racha de aprendizaje de un usuario.
 *
 * @param currentStreak    días consecutivos actuales con actividad válida.
 * @param longestStreak    récord histórico de días consecutivos.
 * @param lastActivityDate fecha (UTC) del último día que contó como actividad.
 * @param streakStartedOn  fecha (UTC) en que comenzó la racha actual.
 * @param updatedAt        fecha de la última actualización del agregado.
 */
public record UserStreakResponse(
        int currentStreak,
        int longestStreak,
        LocalDate lastActivityDate,
        LocalDate streakStartedOn,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir del agregado
     * persistente.
     *
     * @param streak racha del usuario.
     * @return DTO listo para devolver desde la API.
     */
    public static UserStreakResponse from(UserStreak streak) {
        return new UserStreakResponse(
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getLastActivityDate(),
                streak.getStreakStartedOn(),
                streak.getUpdatedAt()
        );
    }
}
