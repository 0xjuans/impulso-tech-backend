package tech.impulso.gamification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.gamification.dto.UserXpResponse;
import tech.impulso.gamification.dto.XpEventResponse;
import tech.impulso.gamification.entity.UserXp;
import tech.impulso.gamification.entity.XpEvent;
import tech.impulso.gamification.repository.XpEventRepository;
import tech.impulso.gamification.service.XpService;
import tech.impulso.users.entity.User;

/**
 * Controlador REST para el sistema de experiencia y niveles (RF-018 /
 * RF-047).
 *
 * <p>Expone consultas del agregado y del historial del usuario
 * autenticado. La escritura ocurre exclusivamente desde los flujos de
 * negocio (por ejemplo, al completar una lección) y no puede ser
 * disparada directamente por el cliente.</p>
 */
@RestController
@RequestMapping("/api/users/me/xp")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Experiencia y niveles",
        description = "Consulta del XP acumulado, el nivel actual y el historial de otorgamientos.")
public class XpController {

    private final XpService xpService;
    private final XpEventRepository xpEventRepository;
    private final CurrentUserService currentUserService;

    public XpController(XpService xpService,
                        XpEventRepository xpEventRepository,
                        CurrentUserService currentUserService) {
        this.xpService = xpService;
        this.xpEventRepository = xpEventRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve el estado actual de XP y nivel del usuario autenticado.
     */
    @Operation(summary = "Consultar mi XP y nivel",
            description = "Devuelve la experiencia total acumulada, el nivel actual y el progreso hacia el siguiente nivel.")
    @Transactional
    @GetMapping
    public ResponseEntity<UserXpResponse> getMyXp() {
        User user = currentUserService.requireAuthenticatedUser();
        UserXp aggregate = xpService.findOrCreate(user);
        int xpForCurrent = xpService.xpRequiredForLevel(aggregate.getCurrentLevel());
        int xpForNext = xpService.xpRequiredForLevel(aggregate.getCurrentLevel() + 1);
        return ResponseEntity.ok(UserXpResponse.from(aggregate, xpForCurrent, xpForNext));
    }

    /**
     * Devuelve el historial paginado de otorgamientos de XP del usuario
     * autenticado.
     */
    @Operation(summary = "Consultar mi historial de XP",
            description = "Devuelve los eventos de XP recibidos por el usuario autenticado, ordenados por fecha.")
    @GetMapping("/history")
    public ResponseEntity<PagedResponse<XpEventResponse>> getMyXpHistory(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        User user = currentUserService.requireAuthenticatedUser();
        Page<XpEvent> page = xpEventRepository.findByUserId(user.getId(), pageable);
        return ResponseEntity.ok(PagedResponse.from(page, XpEventResponse::from));
    }
}
