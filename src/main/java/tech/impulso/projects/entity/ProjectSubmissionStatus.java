package tech.impulso.projects.entity;

/**
 * Estados posibles del ciclo de vida de una entrega de proyecto
 * (RF-023 / RF-045).
 */
public enum ProjectSubmissionStatus {

    /** El estudiante envió la entrega y aún no ha sido revisada. */
    ENVIADA,

    /** El instructor comenzó a revisar la entrega. */
    EN_REVISION,

    /** La entrega fue aprobada por el instructor. */
    APROBADA,

    /**
     * El instructor solicita correcciones; el estudiante puede enviar
     * una nueva versión con los ajustes.
     */
    CORRECCION_SOLICITADA,

    /** La entrega fue rechazada de forma definitiva. */
    RECHAZADA
}
