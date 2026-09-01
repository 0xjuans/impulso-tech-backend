package tech.impulso.activities.dto;

import tech.impulso.activities.entity.Activity;
import tech.impulso.activities.entity.ActivityType;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;

import java.time.OffsetDateTime;

/**
 * Representación pública de una actividad.
 *
 * <p>El campo {@code config} contiene la configuración específica del
 * tipo en formato JSON. Cuando el consumidor es un estudiante, el
 * servicio elimina previamente los campos con las respuestas correctas
 * para no filtrar información sensible.</p>
 *
 * @param id           identificador interno.
 * @param lessonId     identificador de la lección.
 * @param name         nombre visible.
 * @param description  descripción general.
 * @param instructions instrucciones detalladas.
 * @param type         tipo de actividad.
 * @param difficulty   nivel de dificultad.
 * @param maxScore     puntaje máximo obtenible.
 * @param xpReward     XP otorgada al primer acierto.
 * @param maxAttempts  intentos máximos permitidos (nulo = ilimitados).
 * @param orderIndex   posición dentro de la lección.
 * @param status       estado del ciclo de vida.
 * @param config       configuración específica del tipo, en JSON.
 * @param createdAt    fecha de creación.
 * @param updatedAt    fecha de la última modificación.
 */
public record ActivityResponse(
        Long id,
        Long lessonId,
        String name,
        String description,
        String instructions,
        ActivityType type,
        DifficultyLevel difficulty,
        int maxScore,
        int xpReward,
        Integer maxAttempts,
        Integer orderIndex,
        ContentStatus status,
        String config,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir de la entidad.
     *
     * @param activity     entidad a convertir.
     * @param configForApi configuración ya adaptada al consumidor
     *                     (por ejemplo, sin las respuestas correctas
     *                     cuando se envía a un estudiante).
     * @return DTO listo para devolver desde la API.
     */
    public static ActivityResponse from(Activity activity, String configForApi) {
        return new ActivityResponse(
                activity.getId(),
                activity.getLesson().getId(),
                activity.getName(),
                activity.getDescription(),
                activity.getInstructions(),
                activity.getType(),
                activity.getDifficulty(),
                activity.getMaxScore(),
                activity.getXpReward(),
                activity.getMaxAttempts(),
                activity.getOrderIndex(),
                activity.getStatus(),
                configForApi,
                activity.getCreatedAt(),
                activity.getUpdatedAt()
        );
    }
}
