package tech.impulso.challenges.entity;

/**
 * Estados posibles de un intento de reto (RF-014).
 *
 * <p>Mientras no exista el servicio de ejecución automática de código
 * (RF-037), los intentos comienzan en {@link #PENDIENTE} y el instructor
 * los marca como {@link #APROBADO} o {@link #RECHAZADO} tras revisar la
 * solución. Cuando RF-037 esté disponible, la aprobación podrá ser
 * automática al superar los casos de prueba.</p>
 */
public enum ChallengeAttemptStatus {

    /** El intento fue enviado y aún no ha sido revisado. */
    PENDIENTE,

    /** El intento fue aprobado (superó los criterios establecidos). */
    APROBADO,

    /** El intento fue rechazado; el estudiante puede enviar uno nuevo. */
    RECHAZADO
}
