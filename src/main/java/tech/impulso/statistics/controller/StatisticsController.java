package tech.impulso.statistics.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.statistics.dto.InstructorStatisticsResponse;
import tech.impulso.statistics.dto.PlatformStatisticsResponse;
import tech.impulso.statistics.service.StatisticsService;

/**
 * Controlador REST que expone las estadísticas agregadas de la
 * plataforma (RF-058) diferenciadas por rol.
 *
 * <p>Cada endpoint devuelve distribuciones, series temporales y
 * rankings adaptados al alcance del solicitante. Los cálculos se
 * ejecutan bajo demanda; no existen agregaciones precalculadas.</p>
 */
@RestController
@RequestMapping("/api/statistics")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Estadísticas",
        description = "Distribuciones, series temporales y rankings agregados de la plataforma.")
public class StatisticsController {

    private final StatisticsService service;

    public StatisticsController(StatisticsService service) {
        this.service = service;
    }

    /**
     * Estadísticas globales de la plataforma.
     *
     * @param days ventana temporal para las series (1-365, por defecto 30).
     */
    @Operation(summary = "Estadísticas globales de la plataforma",
            description = "Incluye distribuciones, series temporales de registros e inscripciones, "
                    + "y ranking de cursos con más inscripciones.")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping("/platform")
    public ResponseEntity<PlatformStatisticsResponse> platform(
            @Parameter(description = "Cantidad de días de la ventana temporal (1-365).")
            @RequestParam(value = "days", required = false) Integer days) {
        return ResponseEntity.ok(service.getPlatformStatistics(days));
    }

    /**
     * Estadísticas del instructor autenticado.
     *
     * @param days ventana temporal para la serie de inscripciones.
     */
    @Operation(summary = "Estadísticas del instructor",
            description = "Métricas de finalización, evolución de inscripciones y detalle por curso "
                    + "para el instructor autenticado.")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @GetMapping("/instructor")
    public ResponseEntity<InstructorStatisticsResponse> instructor(
            @Parameter(description = "Cantidad de días de la ventana temporal (1-365).")
            @RequestParam(value = "days", required = false) Integer days) {
        return ResponseEntity.ok(service.getInstructorStatistics(days));
    }
}
