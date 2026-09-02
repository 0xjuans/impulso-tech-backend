package tech.impulso.support.entity;

/**
 * Estados posibles del ciclo de vida de un ticket de soporte
 * (RF-036).
 */
public enum SupportTicketStatus {

    /** El ticket fue creado y aún no ha sido tomado por un responsable. */
    PENDIENTE,

    /** El responsable comenzó a revisar el ticket. */
    EN_REVISION,

    /** El responsable está atendiendo la solución del ticket. */
    EN_PROCESO,

    /** El problema fue resuelto. */
    RESUELTO,

    /**
     * El ticket fue cerrado. Puede corresponder a una resolución
     * confirmada por el usuario o a un cierre por parte del
     * administrador cuando no procede ninguna acción adicional.
     */
    CERRADO
}
