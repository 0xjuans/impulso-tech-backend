package tech.impulso.gamification.entity;

/**
 * Condiciones automáticas que disparan el otorgamiento de una insignia
 * (RF-019 / RF-046).
 *
 * <p>Cada disparador se acompaña de un valor umbral almacenado en
 * {@code trigger_value}. Por ejemplo, {@link #LESSONS_COMPLETED_COUNT}
 * junto con valor 10 significa "cuando el estudiante complete 10
 * lecciones obligatorias publicadas".</p>
 */
public enum BadgeTrigger {

    /** El estudiante ha completado al menos {@code trigger_value} lecciones. */
    LESSONS_COMPLETED_COUNT,

    /** El estudiante ha completado al menos {@code trigger_value} cursos. */
    COURSES_COMPLETED_COUNT,

    /** El estudiante ha alcanzado una racha de al menos {@code trigger_value} días consecutivos. */
    STREAK_REACHED,

    /** El estudiante ha acumulado al menos {@code trigger_value} puntos de experiencia. */
    XP_REACHED,

    /** El estudiante ha alcanzado al menos el nivel {@code trigger_value}. */
    LEVEL_REACHED
}
