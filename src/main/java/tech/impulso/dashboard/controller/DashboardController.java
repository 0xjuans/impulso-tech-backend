package tech.impulso.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.dashboard.dto.AdminDashboardResponse;
import tech.impulso.dashboard.dto.InstructorDashboardResponse;
import tech.impulso.dashboard.service.DashboardService;

/**
 * Controlador REST que expone los paneles de instructor (RF-032) y
 * administrador (RF-033).
 *
 * <p>Cada endpoint está protegido por rol mediante {@link PreAuthorize};
 * el servicio devuelve únicamente las métricas correspondientes al rol
 * solicitante.</p>
 */
@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Paneles de control",
        description = "Métricas consolidadas para instructores y administradores.")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    /**
     * Devuelve el panel del instructor autenticado.
     */
    @Operation(summary = "Panel del instructor",
            description = "Métricas consolidadas de los contenidos, inscripciones y tareas pendientes del instructor.")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor")
    public ResponseEntity<InstructorDashboardResponse> instructor() {
        return ResponseEntity.ok(service.getInstructorDashboard());
    }

    /**
     * Devuelve el panel administrativo con las métricas globales de la
     * plataforma.
     */
    @Operation(summary = "Panel del administrador",
            description = "Métricas globales de usuarios, contenidos, inscripciones y tickets.")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/admin")
    public ResponseEntity<AdminDashboardResponse> admin() {
        return ResponseEntity.ok(service.getAdminDashboard());
    }
}
