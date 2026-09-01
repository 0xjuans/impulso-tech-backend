package tech.impulso.gamification.entity;

/**
 * Periodos disponibles para calcular los rankings de estudiantes
 * (RF-021 / RF-049).
 */
public enum RankingPeriod {

    /** Considera la experiencia obtenida en los últimos 7 días. */
    WEEKLY,

    /** Considera la experiencia obtenida en los últimos 30 días. */
    MONTHLY,

    /** Considera la experiencia total acumulada por el usuario. */
    ALL_TIME
}
