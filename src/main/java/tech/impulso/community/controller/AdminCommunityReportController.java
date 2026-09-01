package tech.impulso.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.community.dto.CommunityReportResponse;
import tech.impulso.community.dto.ReviewReportRequest;
import tech.impulso.community.entity.ReportStatus;
import tech.impulso.community.entity.ReportTargetType;
import tech.impulso.community.service.CommunityReportService;

/**
 * Controlador REST administrativo para gestionar los reportes de
 * contenido de la comunidad (RF-034, moderación).
 *
 * <p>Todos los endpoints requieren el rol {@code ADMINISTRADOR}.</p>
 */
@RestController
@RequestMapping("/api/admin/community/reports")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Moderación de comunidad",
        description = "Consulta y resolución de reportes emitidos sobre publicaciones y respuestas.")
public class AdminCommunityReportController {

    private final CommunityReportService service;

    public AdminCommunityReportController(CommunityReportService service) {
        this.service = service;
    }

    /**
     * Devuelve la lista paginada de reportes con filtros opcionales
     * por estado y tipo de contenido.
     */
    @Operation(summary = "Listar reportes",
            description = "Devuelve los reportes registrados con soporte de filtros y paginación.")
    @GetMapping
    public ResponseEntity<PagedResponse<CommunityReportResponse>> list(
            @RequestParam(value = "status", required = false) ReportStatus status,
            @RequestParam(value = "targetType", required = false) ReportTargetType targetType,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listReports(status, targetType, pageable));
    }

    /**
     * Marca un reporte como revisado o desestimado.
     */
    @Operation(summary = "Revisar un reporte",
            description = "Marca el reporte como revisado o desestimado y registra las observaciones internas.")
    @PatchMapping("/{id}")
    public ResponseEntity<CommunityReportResponse> review(@PathVariable("id") Long id,
                                                          @Valid @RequestBody ReviewReportRequest request) {
        return ResponseEntity.ok(service.reviewReport(id, request));
    }
}
