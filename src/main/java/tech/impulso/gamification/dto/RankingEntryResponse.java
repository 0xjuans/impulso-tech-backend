package tech.impulso.gamification.dto;

/**
 * Entrada individual de un ranking de estudiantes.
 *
 * @param position         posición dentro del ranking (base 1).
 * @param userId           identificador del usuario.
 * @param username         nombre de usuario público.
 * @param fullName         nombre completo (nombre y apellido).
 * @param profilePhotoUrl  URL pública de la foto de perfil, si aplica.
 * @param xp               XP considerada para el ranking (histórica o del periodo).
 * @param currentLevel     nivel actual del usuario.
 */
public record RankingEntryResponse(
        long position,
        Long userId,
        String username,
        String fullName,
        String profilePhotoUrl,
        int xp,
        int currentLevel
) {
}
