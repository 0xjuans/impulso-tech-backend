package tech.impulso.support.dto;

import tech.impulso.support.entity.SupportTicket;
import tech.impulso.support.entity.SupportTicketStatus;
import tech.impulso.support.entity.SupportTicketType;

import java.time.OffsetDateTime;

/**
 * Representación pública de un ticket de soporte.
 *
 * @param id               identificador del ticket.
 * @param reporterId       identificador del usuario que reportó.
 * @param reporterName     nombre completo del usuario que reportó.
 * @param type             tipo del ticket.
 * @param title            título breve.
 * @param description      descripción detallada del problema.
 * @param relatedId        identificador del recurso relacionado, si aplica.
 * @param status           estado actual del ticket.
 * @param assigneeId       identificador del responsable asignado, si aplica.
 * @param assigneeName     nombre completo del responsable asignado, si aplica.
 * @param resolutionNotes  notas de resolución registradas por el responsable.
 * @param resolvedAt       momento en que se marcó como resuelto, si aplica.
 * @param createdAt        fecha de creación del ticket.
 * @param updatedAt        fecha de la última actualización.
 */
public record SupportTicketResponse(
        Long id,
        Long reporterId,
        String reporterName,
        SupportTicketType type,
        String title,
        String description,
        Long relatedId,
        SupportTicketStatus status,
        Long assigneeId,
        String assigneeName,
        String resolutionNotes,
        OffsetDateTime resolvedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir de la entidad.
     *
     * @param ticket entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static SupportTicketResponse from(SupportTicket ticket) {
        return new SupportTicketResponse(
                ticket.getId(),
                ticket.getReporter().getId(),
                "%s %s".formatted(ticket.getReporter().getFirstName(), ticket.getReporter().getLastName()),
                ticket.getType(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getRelatedId(),
                ticket.getStatus(),
                ticket.getAssignee() == null ? null : ticket.getAssignee().getId(),
                ticket.getAssignee() == null ? null
                        : "%s %s".formatted(ticket.getAssignee().getFirstName(), ticket.getAssignee().getLastName()),
                ticket.getResolutionNotes(),
                ticket.getResolvedAt(),
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
