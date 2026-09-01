package tech.impulso.gamification.dto;

import tech.impulso.gamification.entity.UserXp;

import java.time.OffsetDateTime;

/**
 * Representación pública del estado de XP y nivel de un usuario.
 *
 * @param totalXp             experiencia total acumulada.
 * @param currentLevel        nivel actual.
 * @param xpForCurrentLevel   XP mínima requerida para el nivel actual.
 * @param xpForNextLevel      XP mínima requerida para alcanzar el siguiente nivel.
 * @param xpIntoCurrentLevel  XP acumulada dentro del nivel actual
 *                            (útil para pintar barras de progreso).
 * @param xpToNextLevel       XP restante para alcanzar el siguiente nivel.
 * @param updatedAt           fecha de la última actualización del agregado.
 */
public record UserXpResponse(
        int totalXp,
        int currentLevel,
        int xpForCurrentLevel,
        int xpForNextLevel,
        int xpIntoCurrentLevel,
        int xpToNextLevel,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir del agregado
     * persistente y de los umbrales calculados por el servicio.
     *
     * @param userXp            agregado del usuario.
     * @param xpForCurrentLevel XP requerida para el nivel actual.
     * @param xpForNextLevel    XP requerida para el siguiente nivel.
     * @return DTO listo para devolver desde la API.
     */
    public static UserXpResponse from(UserXp userXp, int xpForCurrentLevel, int xpForNextLevel) {
        int totalXp = userXp.getTotalXp();
        int intoCurrent = totalXp - xpForCurrentLevel;
        int toNext = Math.max(0, xpForNextLevel - totalXp);
        return new UserXpResponse(
                totalXp,
                userXp.getCurrentLevel(),
                xpForCurrentLevel,
                xpForNextLevel,
                intoCurrent,
                toNext,
                userXp.getUpdatedAt()
        );
    }
}
