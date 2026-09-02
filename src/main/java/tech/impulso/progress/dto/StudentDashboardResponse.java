package tech.impulso.progress.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Vista consolidada del avance académico y de gamificación del usuario
 * autenticado (RF-029).
 *
 * @param totalXp                experiencia total acumulada.
 * @param currentLevel           nivel actual.
 * @param xpForCurrentLevel      XP mínima requerida para el nivel actual.
 * @param xpForNextLevel         XP mínima requerida para el siguiente nivel.
 * @param xpToNextLevel          XP restante para alcanzar el siguiente nivel.
 * @param currentStreak          días consecutivos actuales con actividad válida.
 * @param longestStreak          récord histórico de racha.
 * @param streakStartedOn        fecha de inicio de la racha actual.
 * @param lastActivityDate       fecha del último día que contó como actividad.
 * @param coursesEnrolled        cantidad de cursos en los que el estudiante está inscrito.
 * @param coursesCompleted       cantidad de cursos completados.
 * @param lessonsCompleted       cantidad total de lecciones completadas.
 * @param challengesSolved       cantidad de retos distintos resueltos.
 * @param projectsApproved       cantidad de proyectos con al menos una entrega aprobada.
 * @param badgesEarned           cantidad de insignias obtenidas.
 * @param unreadNotifications    cantidad de notificaciones pendientes de leer.
 * @param generatedAt            momento en que se generó el resumen.
 */
public record StudentDashboardResponse(
        int totalXp,
        int currentLevel,
        int xpForCurrentLevel,
        int xpForNextLevel,
        int xpToNextLevel,
        int currentStreak,
        int longestStreak,
        LocalDate streakStartedOn,
        LocalDate lastActivityDate,
        long coursesEnrolled,
        long coursesCompleted,
        long lessonsCompleted,
        long challengesSolved,
        long projectsApproved,
        long badgesEarned,
        long unreadNotifications,
        OffsetDateTime generatedAt
) {
}
