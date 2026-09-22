package tech.impulso.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import tech.impulso.common.security.CurrentUserService;
import tech.impulso.notifications.service.NotificationSseBroadcaster;
import tech.impulso.users.entity.User;

/**
 * Controlador REST que expone el canal de notificaciones en tiempo real
 * mediante {@code Server-Sent Events} (RF-026).
 *
 * <p>El cliente abre una única conexión hacia
 * {@code /api/users/me/notifications/stream} y recibe eventos
 * asíncronos cada vez que se genera una notificación para el usuario
 * autenticado. Complementa al endpoint HTTP tradicional del
 * {@link NotificationController} sin sustituirlo: el listado y el
 * conteo siguen sirviéndose por REST, mientras que este stream sólo
 * empuja las nuevas notificaciones.</p>
 *
 * <p>La autenticación se resuelve en {@code JwtAuthenticationFilter}: al
 * ser un endpoint consumido por {@code EventSource}, el token viaja como
 * parámetro de consulta {@code access_token} en lugar del encabezado
 * {@code Authorization}.</p>
 */
@RestController
@RequestMapping("/api/users/me/notifications")
@Tag(name = "Notificaciones en tiempo real",
        description = "Canal Server-Sent Events con nuevas notificaciones del usuario autenticado.")
public class NotificationStreamController {

    private final NotificationSseBroadcaster broadcaster;
    private final CurrentUserService currentUserService;

    public NotificationStreamController(NotificationSseBroadcaster broadcaster,
                                        CurrentUserService currentUserService) {
        this.broadcaster = broadcaster;
        this.currentUserService = currentUserService;
    }

    /**
     * Abre una conexión SSE que recibirá las notificaciones nuevas del
     * usuario autenticado en tiempo real. El servidor cerrará la
     * conexión al agotar su tiempo máximo; el cliente debe volver a
     * conectar cuando esto ocurra.
     */
    @Operation(summary = "Stream de notificaciones",
            description = "Abre una conexión Server-Sent Events. El evento 'notification' entrega el DTO de la notificación recién creada.")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        User user = currentUserService.requireAuthenticatedUser();
        return broadcaster.subscribe(user.getId());
    }
}
