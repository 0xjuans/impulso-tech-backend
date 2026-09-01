package tech.impulso.gamification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.gamification.dto.BadgeResponse;
import tech.impulso.gamification.dto.UserBadgeResponse;
import tech.impulso.gamification.service.BadgeService;
import tech.impulso.users.entity.User;

import java.util.List;

/**
 * Controlador REST para el catálogo de insignias y las insignias
 * obtenidas por el usuario autenticado (RF-019 / RF-046).
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Insignias", description = "Catálogo de insignias e insignias obtenidas por el usuario.")
public class BadgeController {

    private final BadgeService badgeService;
    private final CurrentUserService currentUserService;

    public BadgeController(BadgeService badgeService, CurrentUserService currentUserService) {
        this.badgeService = badgeService;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve el catálogo de insignias activas de la plataforma.
     */
    @Operation(summary = "Listar el catálogo de insignias",
            description = "Devuelve todas las insignias disponibles y sus condiciones de obtención.")
    @GetMapping("/badges")
    public ResponseEntity<List<BadgeResponse>> listCatalog() {
        return ResponseEntity.ok(badgeService.listCatalog().stream()
                .map(BadgeResponse::from)
                .toList());
    }

    /**
     * Devuelve las insignias obtenidas por el usuario autenticado.
     */
    @Operation(summary = "Listar mis insignias",
            description = "Devuelve las insignias que el usuario autenticado ha obtenido, ordenadas por fecha.")
    @GetMapping("/users/me/badges")
    public ResponseEntity<List<UserBadgeResponse>> listMine() {
        User user = currentUserService.requireAuthenticatedUser();
        return ResponseEntity.ok(badgeService.listUserBadges(user.getId()).stream()
                .map(UserBadgeResponse::from)
                .toList());
    }
}
