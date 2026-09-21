package tech.impulso.dashboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import tech.impulso.dashboard.dto.AdminDashboardResponse;
import tech.impulso.dashboard.dto.InstructorDashboardResponse;
import tech.impulso.dashboard.dto.RecentEnrollmentRow;
import tech.impulso.dashboard.dto.RecentSignupRow;
import tech.impulso.dashboard.dto.TopCourseRow;
import tech.impulso.dashboard.service.DashboardService;

import java.util.List;

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

    @Operation(summary = "Cursos con más inscripciones del instructor",
            description = "Top cursos del instructor autenticado con total y finalizadas para calcular la tasa de finalización.")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/top-courses")
    public ResponseEntity<List<TopCourseRow>> instructorTopCourses(
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(service.getInstructorTopCourses(limit));
    }

    @Operation(summary = "Últimas inscripciones a mis cursos",
            description = "Feed de las inscripciones más recientes en cursos del instructor autenticado.")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor/recent-enrollments")
    public ResponseEntity<List<RecentEnrollmentRow>> instructorRecentEnrollments(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getInstructorRecentEnrollments(limit));
    }

    @Operation(summary = "Cursos globales con más inscripciones",
            description = "Top cursos por inscripciones para el panel del administrador.")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/admin/top-courses")
    public ResponseEntity<List<TopCourseRow>> adminTopCourses(
            @RequestParam(name = "limit", defaultValue = "5") int limit) {
        return ResponseEntity.ok(service.getAdminTopCourses(limit));
    }

    @Operation(summary = "Últimas inscripciones globales",
            description = "Feed de las inscripciones más recientes en toda la plataforma.")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/admin/recent-enrollments")
    public ResponseEntity<List<RecentEnrollmentRow>> adminRecentEnrollments(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getAdminRecentEnrollments(limit));
    }

    @Operation(summary = "Últimos usuarios registrados",
            description = "Feed de las cuentas más recientes con su rol y estado de verificación.")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/admin/recent-signups")
    public ResponseEntity<List<RecentSignupRow>> adminRecentSignups(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(service.getAdminRecentSignups(limit));
    }
}
