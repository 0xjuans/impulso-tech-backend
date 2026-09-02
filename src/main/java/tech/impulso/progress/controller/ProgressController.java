package tech.impulso.progress.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.progress.dto.ActivityHistoryEntryResponse;
import tech.impulso.progress.dto.StudentDashboardResponse;
import tech.impulso.progress.service.ProgressService;

/**
 * Controlador REST que consolida el panel de progreso (RF-029) y el
 * historial de actividad (RF-028) del usuario autenticado.
 */
@RestController
@RequestMapping("/api/users/me")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Progreso y actividad",
        description = "Vistas agregadas del avance académico y el historial de eventos del usuario.")
public class ProgressController {

    private final ProgressService service;

    public ProgressController(ProgressService service) {
        this.service = service;
    }

    /**
     * Devuelve el panel de progreso del usuario autenticado con las
     * principales métricas de aprendizaje y gamificación.
     */
    @Operation(summary = "Consultar mi panel de progreso",
            description = "Devuelve un resumen consolidado del avance académico, XP, nivel, racha e insignias.")
    @GetMapping("/dashboard")
    public ResponseEntity<StudentDashboardResponse> getDashboard() {
        return ResponseEntity.ok(service.buildDashboard());
    }

    /**
     * Devuelve el historial paginado de actividades y logros del
     * usuario autenticado, ordenado del más reciente al más antiguo.
     */
    @Operation(summary = "Consultar mi historial de actividad",
            description = "Devuelve una línea de tiempo consolidada de los eventos de aprendizaje y gamificación del usuario.")
    @GetMapping("/history")
    public ResponseEntity<PagedResponse<ActivityHistoryEntryResponse>> getHistory(
            @PageableDefault(size = 30) Pageable pageable) {
        return ResponseEntity.ok(service.listMyHistory(pageable));
    }
}
