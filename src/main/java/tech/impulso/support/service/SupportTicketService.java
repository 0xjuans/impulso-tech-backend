package tech.impulso.support.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.notifications.entity.NotificationType;
import tech.impulso.notifications.service.NotificationService;
import tech.impulso.support.dto.CreateTicketRequest;
import tech.impulso.support.dto.SupportTicketResponse;
import tech.impulso.support.dto.UpdateTicketRequest;
import tech.impulso.support.entity.SupportTicket;
import tech.impulso.support.entity.SupportTicketStatus;
import tech.impulso.support.entity.SupportTicketType;
import tech.impulso.support.repository.SupportTicketRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.repository.UserRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Servicio con las operaciones sobre tickets de soporte (RF-036).
 *
 * <p>Cubre la creación de tickets por parte de los usuarios y su
 * gestión por parte del instructor asignado o del administrador. Cada
 * cambio de estado dispara una notificación al usuario que reportó.</p>
 */
@Service
public class SupportTicketService {

    /** Valor sentinela para desasignar el responsable de un ticket. */
    private static final long UNASSIGN_SENTINEL = -1L;

    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public SupportTicketService(SupportTicketRepository ticketRepository,
                                UserRepository userRepository,
                                CurrentUserService currentUserService,
                                NotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    /**
     * Crea un nuevo ticket a nombre del usuario autenticado.
     */
    @Transactional
    public SupportTicketResponse create(CreateTicketRequest request) {
        User reporter = currentUserService.requireAuthenticatedUser();

        SupportTicket ticket = new SupportTicket();
        ticket.setReporter(reporter);
        ticket.setType(request.type());
        ticket.setTitle(request.title().trim());
        ticket.setDescription(request.description());
        ticket.setRelatedId(request.relatedId());
        ticket.setStatus(SupportTicketStatus.PENDIENTE);
        return SupportTicketResponse.from(ticketRepository.save(ticket));
    }

    /**
     * Devuelve los tickets creados por el usuario autenticado.
     */
    @Transactional(readOnly = true)
    public PagedResponse<SupportTicketResponse> listMine(SupportTicketStatus status,
                                                         Pageable pageable) {
        User user = currentUserService.requireAuthenticatedUser();
        Page<SupportTicket> page = ticketRepository.search(user.getId(), null, status, null, pageable);
        return PagedResponse.from(page, SupportTicketResponse::from);
    }

    /**
     * Consulta el detalle de un ticket. El usuario autenticado debe ser
     * el autor, el responsable asignado o un administrador.
     */
    @Transactional(readOnly = true)
    public SupportTicketResponse findById(Long id) {
        SupportTicket ticket = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanRead(ticket, current);
        return SupportTicketResponse.from(ticket);
    }

    /**
     * Devuelve una página con los tickets registrados con soporte de
     * filtros. Cuando el consumidor es un instructor sólo se
     * incluyen los tickets que tenga asignados.
     */
    @Transactional(readOnly = true)
    public PagedResponse<SupportTicketResponse> listForStaff(SupportTicketStatus status,
                                                             SupportTicketType type,
                                                             Long assigneeId,
                                                             Pageable pageable) {
        User current = currentUserService.requireAuthenticatedUser();
        Long effectiveAssignee = current.getRole() == Role.ADMINISTRADOR ? assigneeId : current.getId();
        Page<SupportTicket> page = ticketRepository.search(null, effectiveAssignee, status, type, pageable);
        return PagedResponse.from(page, SupportTicketResponse::from);
    }

    /**
     * Actualiza el estado, la asignación o las notas de resolución de
     * un ticket. Sólo puede ejecutarla el administrador o el
     * responsable asignado.
     */
    @Transactional
    public SupportTicketResponse update(Long id, UpdateTicketRequest request) {
        SupportTicket ticket = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanManage(ticket, current);

        SupportTicketStatus previousStatus = ticket.getStatus();

        if (request.assigneeId() != null) {
            if (request.assigneeId() == UNASSIGN_SENTINEL) {
                ticket.setAssignee(null);
            } else {
                User assignee = userRepository.findById(request.assigneeId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                "El responsable indicado no existe."));
                if (assignee.getRole() == Role.ESTUDIANTE) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST,
                            "El responsable asignado debe ser instructor o administrador.");
                }
                ticket.setAssignee(assignee);
            }
        }
        if (request.resolutionNotes() != null) {
            ticket.setResolutionNotes(request.resolutionNotes());
        }
        if (request.status() != null && request.status() != previousStatus) {
            ticket.setStatus(request.status());
            if (request.status() == SupportTicketStatus.RESUELTO && ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(OffsetDateTime.now(ZoneOffset.UTC));
            }
            notificationService.notify(
                    ticket.getReporter(),
                    NotificationType.GENERIC,
                    "Actualización de tu ticket #%d".formatted(ticket.getId()),
                    "El estado de tu ticket \"%s\" cambió a %s."
                            .formatted(ticket.getTitle(), request.status()),
                    "SUPPORT_TICKET",
                    ticket.getId()
            );
        }

        return SupportTicketResponse.from(ticket);
    }

    /**
     * Carga un ticket por identificador o lanza 404.
     */
    private SupportTicket load(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El ticket indicado no existe."));
    }

    /**
     * Verifica que el usuario tenga permisos para consultar el ticket.
     */
    private void ensureCanRead(SupportTicket ticket, User user) {
        if (user.getRole() == Role.ADMINISTRADOR) {
            return;
        }
        if (ticket.getReporter().getId().equals(user.getId())) {
            return;
        }
        if (ticket.getAssignee() != null && ticket.getAssignee().getId().equals(user.getId())) {
            return;
        }
        throw new BusinessException(HttpStatus.NOT_FOUND, "El ticket indicado no existe.");
    }

    /**
     * Verifica que el usuario tenga permisos para modificar el ticket.
     */
    private void ensureCanManage(SupportTicket ticket, User user) {
        if (user.getRole() == Role.ADMINISTRADOR) {
            return;
        }
        if (user.getRole() == Role.INSTRUCTOR
                && ticket.getAssignee() != null
                && ticket.getAssignee().getId().equals(user.getId())) {
            return;
        }
        throw new BusinessException(HttpStatus.FORBIDDEN,
                "Solo el responsable asignado o un administrador puede gestionar este ticket.");
    }
}
