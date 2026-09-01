package tech.impulso.evaluations.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Datos que se pueden modificar de una evaluación existente. Los valores
 * {@code null} indican que el campo no debe actualizarse.
 *
 * @param name               nuevo nombre.
 * @param description        nueva descripción.
 * @param instructions       nuevas instrucciones.
 * @param timeLimitMinutes   nuevo tiempo límite.
 * @param passingPercentage  nuevo porcentaje mínimo.
 * @param maxAttempts        nueva cantidad máxima de intentos.
 * @param orderIndex         nueva posición dentro de la lección.
 */
public record UpdateEvaluationRequest(
        @Size(max = 180) String name,
        String description,
        String instructions,
        @Positive Integer timeLimitMinutes,
        @Min(0) @Max(100) Integer passingPercentage,
        @Positive Integer maxAttempts,
        @Positive Integer orderIndex
) {
}
