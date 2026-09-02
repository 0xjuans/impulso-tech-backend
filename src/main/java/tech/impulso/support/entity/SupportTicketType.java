package tech.impulso.support.entity;

/**
 * Tipos de problemas soportados por el sistema de tickets (RF-036).
 *
 * <p>Cuando el tipo corresponde a un contenido educativo, el campo
 * {@code relatedId} del ticket apunta al recurso concreto.</p>
 */
public enum SupportTicketType {

    /** Error técnico general de la plataforma (no vinculado a contenido). */
    TECHNICAL_ERROR,

    /** Problema con un curso. */
    COURSE_ISSUE,

    /** Problema con una lección. */
    LESSON_ISSUE,

    /** Problema con una actividad. */
    ACTIVITY_ISSUE,

    /** Problema con un reto. */
    CHALLENGE_ISSUE,

    /** Problema con un laboratorio. */
    LAB_ISSUE,

    /** Problema con una evaluación. */
    EVALUATION_ISSUE,

    /** Problema con un proyecto. */
    PROJECT_ISSUE,

    /** Problema con un recurso educativo. */
    RESOURCE_ISSUE,

    /** Problema con la mascota virtual con inteligencia artificial. */
    AI_MASCOT_ISSUE,

    /** Otro tipo de problema no cubierto por las categorías anteriores. */
    OTHER
}
