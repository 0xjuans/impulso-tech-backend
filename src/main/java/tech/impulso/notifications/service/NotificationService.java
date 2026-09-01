package tech.impulso.notifications.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.notifications.dto.NotificationResponse;
import tech.impulso.notifications.entity.Notification;
import tech.impulso.notifications.entity.NotificationType;
import tech.impulso.notifications.repository.NotificationRepository;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Servicio responsable de crear y consultar notificaciones de usuario
 * (RF-026).
 *
 * <p>La creación se ejecuta con propagación {@link Propagation#REQUIRES_NEW}
 * para que el registro de la notificación no se cancele si la operación
 * de negocio principal realiza rollback. Esto asegura que el estudiante
 * reciba oportunamente la información aunque otra parte del flujo
 * fracase.</p>
 */
@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final CurrentUserService currentUserService;

    public NotificationService(NotificationRepository repository,
                               CurrentUserService currentUserService) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    /**
     * Crea una nueva notificación para el usuario indicado.
     *
     * @param user        destinatario.
     * @param type        tipo funcional.
     * @param title       título breve.
     * @param message     mensaje detallado.
     * @param relatedType tipo del recurso relacionado, si aplica.
     * @param relatedId   identificador del recurso relacionado, si aplica.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notify(User user,
                       NotificationType type,
                       String title,
                       String message,
                       String relatedType,
                       Long relatedId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        repository.save(notification);
    }

    /**
     * Devuelve las notificaciones del usuario autenticado.
     *
     * @param onlyUnread indica si sólo se deben devolver las no leídas.
     * @param pageable   configuración de paginación y ordenamiento.
     * @return página con las notificaciones del usuario.
     */
    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> listMine(boolean onlyUnread, Pageable pageable) {
        User user = currentUserService.requireAuthenticatedUser();
        Page<Notification> page = repository.findForUser(user.getId(), onlyUnread, pageable);
        return PagedResponse.from(page, NotificationResponse::from);
    }

    /**
     * Devuelve la cantidad de notificaciones sin leer del usuario
     * autenticado.
     *
     * @return conteo de no leídas.
     */
    @Transactional(readOnly = true)
    public long countMineUnread() {
        User user = currentUserService.requireAuthenticatedUser();
        return repository.countUnread(user.getId());
    }

    /**
     * Marca una notificación específica como leída.
     *
     * @param notificationId identificador de la notificación.
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        User user = currentUserService.requireAuthenticatedUser();
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "La notificación indicada no existe."));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "No puede modificar notificaciones de otro usuario.");
        }
        if (notification.getReadAt() == null) {
            notification.setReadAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
    }

    /**
     * Marca todas las notificaciones no leídas del usuario autenticado
     * como leídas.
     *
     * @return cantidad de notificaciones actualizadas.
     */
    @Transactional
    public int markAllAsRead() {
        User user = currentUserService.requireAuthenticatedUser();
        return repository.markAllAsRead(user.getId(), OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Elimina una notificación del centro del usuario autenticado.
     *
     * @param notificationId identificador de la notificación.
     */
    @Transactional
    public void delete(Long notificationId) {
        User user = currentUserService.requireAuthenticatedUser();
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "La notificación indicada no existe."));
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "No puede eliminar notificaciones de otro usuario.");
        }
        repository.delete(notification);
    }
}
