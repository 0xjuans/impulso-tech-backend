package tech.impulso.support.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.support.entity.SupportTicket;
import tech.impulso.support.entity.SupportTicketStatus;
import tech.impulso.support.entity.SupportTicketType;

/**
 * Repositorio de persistencia para {@link SupportTicket}.
 */
@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    /**
     * Búsqueda paginada de tickets aplicando filtros opcionales.
     *
     * @param reporterId identificador del usuario que reportó, opcional.
     * @param assigneeId identificador del responsable asignado, opcional.
     * @param status     estado a filtrar, opcional.
     * @param type       tipo a filtrar, opcional.
     * @param pageable   configuración de paginación y ordenamiento.
     * @return página con los tickets coincidentes.
     */
    @Query("""
            select t from SupportTicket t
            where (:reporterId is null or t.reporter.id = :reporterId)
              and (:assigneeId is null or t.assignee.id = :assigneeId)
              and (:status     is null or t.status      = :status)
              and (:type       is null or t.type        = :type)
            """)
    Page<SupportTicket> search(@Param("reporterId") Long reporterId,
                               @Param("assigneeId") Long assigneeId,
                               @Param("status") SupportTicketStatus status,
                               @Param("type") SupportTicketType type,
                               Pageable pageable);

    /**
     * Cuenta cuántos tickets existen en el estado indicado. Se utiliza
     * en el panel del administrador (RF-033).
     */
    long countByStatus(SupportTicketStatus status);

    /**
     * Cuenta cuántos tickets están asignados al responsable indicado en
     * el estado suministrado. Se utiliza en el panel del instructor
     * (RF-032) cuando actúa como resolutor.
     */
    long countByAssigneeIdAndStatus(Long assigneeId, SupportTicketStatus status);
}
