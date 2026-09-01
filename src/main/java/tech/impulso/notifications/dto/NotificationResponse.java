package tech.impulso.notifications.dto;

import tech.impulso.notifications.entity.Notification;
import tech.impulso.notifications.entity.NotificationType;

import java.time.OffsetDateTime;

/**
 * Representación pública de una notificación.
 *
 * @param id          identificador interno.
 * @param type        tipo funcional.
 * @param title       título breve.
 * @param message     mensaje detallado.
 * @param relatedType tipo del recurso relacionado, si aplica.
 * @param relatedId   identificador del recurso relacionado, si aplica.
 * @param read        indica si la notificación ya fue leída.
 * @param readAt      momento en que se leyó, si aplica.
 * @param createdAt   fecha y hora en que se generó.
 */
public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String relatedType,
        Long relatedId,
        boolean read,
        OffsetDateTime readAt,
        OffsetDateTime createdAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param notification entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getRelatedType(),
                notification.getRelatedId(),
                notification.getReadAt() != null,
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
