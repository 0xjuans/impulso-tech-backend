package tech.impulso.notifications.dto;

/**
 * Respuesta con el conteo de notificaciones sin leer del usuario
 * autenticado.
 *
 * @param unread cantidad de notificaciones sin leer.
 */
public record UnreadCountResponse(long unread) {
}
