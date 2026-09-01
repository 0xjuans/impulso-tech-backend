package tech.impulso.activities.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import tech.impulso.activities.entity.ActivityType;
import tech.impulso.common.content.DifficultyLevel;

/**
 * Datos requeridos para crear una nueva actividad dentro de una lección.
 *
 * <p>El campo {@code config} contiene la configuración específica del
 * tipo en formato JSON. El servicio la valida antes de persistir.</p>
 *
 * @param name         nombre visible.
 * @param description  descripción general.
 * @param instructions instrucciones detalladas.
 * @param type         tipo de actividad.
 * @param difficulty   nivel de dificultad.
 * @param maxScore     puntaje máximo (mayor que cero).
 * @param xpReward     XP otorgada al primer acierto (cero o positivo).
 * @param maxAttempts  intentos máximos (nulo = ilimitados).
 * @param orderIndex   posición dentro de la lección; se autoasigna si es nulo.
 * @param config       configuración específica del tipo, en JSON.
 */
public record CreateActivityRequest(
        @NotBlank(message = "El nombre de la actividad es obligatorio.")
        @Size(max = 180)
        String name,

        String description,

        String instructions,

        @NotNull(message = "El tipo de actividad es obligatorio.")
        ActivityType type,

        @NotNull(message = "El nivel de dificultad es obligatorio.")
        DifficultyLevel difficulty,

        @Positive(message = "El puntaje máximo debe ser mayor que cero.")
        Integer maxScore,

        @PositiveOrZero(message = "La XP otorgada no puede ser negativa.")
        Integer xpReward,

        @Positive(message = "El número máximo de intentos debe ser mayor que cero.")
        Integer maxAttempts,

        @Positive(message = "La posición debe ser mayor que cero.")
        Integer orderIndex,

        @NotBlank(message = "La configuración de la actividad es obligatoria.")
        String config
) {
}
