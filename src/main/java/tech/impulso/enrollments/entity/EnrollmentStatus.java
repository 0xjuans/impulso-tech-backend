package tech.impulso.enrollments.entity;

/**
 * Estados posibles del avance de un estudiante dentro de un curso
 * (RF-011).
 */
public enum EnrollmentStatus {

    /** El estudiante se ha inscrito pero aún no ha completado ninguna actividad. */
    INSCRITO,

    /** El estudiante ha completado al menos una actividad del curso. */
    EN_PROGRESO,

    /**
     * El estudiante ha cumplido las condiciones de finalización del curso
     * (todas las lecciones obligatorias completadas).
     */
    COMPLETADO
}
