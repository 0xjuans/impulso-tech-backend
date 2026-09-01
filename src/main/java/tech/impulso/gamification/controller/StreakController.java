package tech.impulso.gamification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.gamification.dto.UserStreakResponse;
import tech.impulso.gamification.service.StreakService;
import tech.impulso.users.entity.User;

/**
 * Controlador REST para la racha de aprendizaje del usuario autenticado
 * (RF-020 / RF-048).
 */
@RestController
@RequestMapping("/api/users/me/streak")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Racha de aprendizaje",
        description = "Consulta de la racha actual y el récord histórico del usuario autenticado.")
public class StreakController {

    private final StreakService streakService;
    private final CurrentUserService currentUserService;

    public StreakController(StreakService streakService, CurrentUserService currentUserService) {
        this.streakService = streakService;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve la racha actual y el récord histórico del usuario
     * autenticado.
     */
    @Operation(summary = "Consultar mi racha de aprendizaje",
            description = "Devuelve la racha actual, el récord histórico y la fecha del último día que contó como actividad válida.")
    @GetMapping
    public ResponseEntity<UserStreakResponse> getMyStreak() {
        User user = currentUserService.requireAuthenticatedUser();
        return ResponseEntity.ok(UserStreakResponse.from(streakService.findOrCreate(user)));
    }
}
