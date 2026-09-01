package tech.impulso.gamification.entity;

/**
 * Nivel de rareza de una insignia. Sirve como pista visual del esfuerzo
 * requerido para obtenerla.
 */
public enum BadgeRarity {

    /** Insignia común, alcanzable con actividad regular. */
    COMUN,

    /** Insignia poco común, alcanzable con cierto compromiso. */
    RARA,

    /** Insignia épica, alcanzable con dedicación notable. */
    EPICA,

    /** Insignia legendaria, reservada a logros excepcionales. */
    LEGENDARIA
}
