package tech.impulso.notifications.entity;

/**
 * Tipos funcionales de notificaciones generadas por Impulso Tech
 * (RF-026).
 */
public enum NotificationType {

    /** El estudiante obtuvo una nueva insignia. */
    BADGE_AWARDED,

    /** El estudiante subió de nivel dentro del sistema de gamificación. */
    LEVEL_UP,

    /** El estudiante completó un curso. */
    COURSE_COMPLETED,

    /** El estudiante aprobó una evaluación. */
    EVALUATION_PASSED,

    /** El estudiante no aprobó una evaluación. */
    EVALUATION_FAILED,

    /**
     * El estudiante alcanzó un hito importante en su racha de
     * aprendizaje (por ejemplo, 7, 30 o 100 días consecutivos).
     */
    STREAK_MILESTONE,

    /**
     * Notificación genérica no vinculada a un evento del sistema (por
     * ejemplo, mensajes administrativos o recordatorios).
     */
    GENERIC
}
