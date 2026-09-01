package tech.impulso.gamification.entity;

/**
 * Fuentes válidas de otorgamiento de experiencia (XP) dentro de la
 * plataforma (RF-018).
 *
 * <p>Cada fuente identifica el tipo de recurso que originó la
 * recompensa. Junto con el identificador del recurso permite garantizar
 * que un mismo evento no otorgue experiencia más de una vez al mismo
 * estudiante.</p>
 */
public enum XpSource {

    /** El estudiante completó una lección publicada. */
    LESSON_COMPLETED,

    /** El estudiante completó todas las lecciones obligatorias de un curso. */
    COURSE_COMPLETED,

    /**
     * El estudiante respondió correctamente una actividad por primera
     * vez. La cantidad de XP la define el instructor al crear la
     * actividad.
     */
    ACTIVITY_COMPLETED,

    /**
     * El estudiante recibió por primera vez la aprobación de una
     * entrega de proyecto. La cantidad de XP la define el instructor
     * al crear el proyecto.
     */
    PROJECT_APPROVED,

    /**
     * El estudiante resolvió correctamente un reto de programación
     * por primera vez. La cantidad de XP la define el instructor al
     * crear el reto.
     */
    CHALLENGE_SOLVED
}
