package tech.impulso.notifications.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.notifications.dto.NotificationResponse;
import tech.impulso.notifications.dto.UnreadCountResponse;
import tech.impulso.notifications.service.NotificationService;

/**
 * Controlador REST del centro de notificaciones del usuario autenticado
 * (RF-026).
 */
@RestController
@RequestMapping("/api/users/me/notifications")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notificaciones",
        description = "Centro de notificaciones del usuario autenticado.")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    /**
     * Devuelve las notificaciones del usuario autenticado.
     */
    @Operation(summary = "Listar mis notificaciones",
            description = "Devuelve las notificaciones del usuario autenticado con soporte de paginación y filtro por leídas/no leídas.")
    @GetMapping
    public ResponseEntity<PagedResponse<NotificationResponse>> list(
            @RequestParam(value = "onlyUnread", defaultValue = "false") boolean onlyUnread,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listMine(onlyUnread, pageable));
    }

    /**
     * Devuelve la cantidad de notificaciones sin leer del usuario.
     */
    @Operation(summary = "Contar mis notificaciones sin leer",
            description = "Devuelve la cantidad de notificaciones sin leer del usuario autenticado.")
    @GetMapping("/unread-count")
    public ResponseEntity<UnreadCountResponse> unreadCount() {
        return ResponseEntity.ok(new UnreadCountResponse(service.countMineUnread()));
    }

    /**
     * Marca una notificación específica como leída.
     */
    @Operation(summary = "Marcar una notificación como leída")
    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        service.markAsRead(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marca todas las notificaciones no leídas como leídas.
     */
    @Operation(summary = "Marcar todas las notificaciones como leídas")
    @PatchMapping("/read-all")
    public ResponseEntity<UnreadCountResponse> markAllAsRead() {
        int updated = service.markAllAsRead();
        return ResponseEntity.ok(new UnreadCountResponse(updated));
    }

    /**
     * Elimina una notificación del centro del usuario.
     */
    @Operation(summary = "Eliminar una notificación")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
