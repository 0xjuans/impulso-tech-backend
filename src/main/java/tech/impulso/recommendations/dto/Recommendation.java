package tech.impulso.recommendations.dto;

/**
 * Recomendación individual devuelta por el motor de sugerencias
 * (RF-053).
 *
 * @param id       identificador de la entidad recomendada.
 * @param type     tipo lógico ({@code COURSE}, {@code ROUTE},
 *                 {@code CHALLENGE}, {@code RESOURCE}).
 * @param title    título visible.
 * @param subtitle descripción breve o metadato secundario.
 * @param reason   justificación textual generada por el motor.
 * @param score    puntaje interno con el que se ordenó la recomendación.
 */
public record Recommendation(
        Long id,
        String type,
        String title,
        String subtitle,
        String reason,
        int score
) {
}
