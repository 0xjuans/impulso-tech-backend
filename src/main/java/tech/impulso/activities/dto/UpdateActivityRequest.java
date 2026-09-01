package tech.impulso.activities.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tech.impulso.common.content.DifficultyLevel;

/**
 * Datos que se pueden modificar de una actividad existente. Los valores
 * {@code null} indican que el campo no debe actualizarse.
 *
 * <p>El tipo de actividad no se puede cambiar tras la creación; para
 * cambiarlo se debe eliminar y volver a crear la actividad.</p>
 *
 * @param name         nuevo nombre.
 * @param description  nueva descripción.
 * @param instructions nuevas instrucciones.
 * @param difficulty   nuevo nivel de dificultad.
 * @param maxScore     nuevo puntaje máximo.
 * @param xpReward     nueva recompensa de XP.
 * @param maxAttempts  nuevo límite de intentos; enviar cero para volver
 *                     a intentos ilimitados no está soportado, use el
 *                     endpoint de eliminar y recrear.
 * @param orderIndex   nueva posición dentro de la lección.
 * @param config       nueva configuración específica del tipo.
 */
public record UpdateActivityRequest(
        @Size(max = 180) String name,
        String description,
        String instructions,
        DifficultyLevel difficulty,
        @Positive Integer maxScore,
        @PositiveOrZero Integer xpReward,
        @Positive Integer maxAttempts,
        @Positive Integer orderIndex,
        String config
) {
}
