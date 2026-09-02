package tech.impulso.support.dto;

import tech.impulso.support.entity.SupportTicketStatus;

/**
 * Solicitud del responsable para actualizar el estado, la asignación o
 * las notas de resolución de un ticket. Todos los campos son
 * opcionales: los valores nulos indican que ese campo no se modifica.
 *
 * <p>Para desasignar un ticket previamente asignado se puede enviar
 * {@code -1} en {@code assigneeId}.</p>
 *
 * @param status          nuevo estado del ticket.
 * @param assigneeId      nuevo responsable asignado (o {@code -1} para desasignar).
 * @param resolutionNotes notas de resolución para el usuario.
 */
public record UpdateTicketRequest(
        SupportTicketStatus status,
        Long assigneeId,
        String resolutionNotes
) {
}
