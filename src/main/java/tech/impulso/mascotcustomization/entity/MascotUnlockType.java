package tech.impulso.mascotcustomization.entity;

/**
 * Criterio de desbloqueo de un ítem de la mascota (RF-022, RF-050).
 *
 * <p>La plataforma nunca utiliza una moneda interna; los ítems se
 * conceden automáticamente al cumplir el requisito asociado.</p>
 */
public enum MascotUnlockType {

    /** Ítem disponible para todos los estudiantes desde su registro. */
    DEFAULT,

    /** Requiere alcanzar un nivel mínimo. */
    LEVEL,

    /** Requiere acumular una cantidad mínima de XP. */
    XP,

    /** Requiere obtener una insignia específica. */
    BADGE
}
