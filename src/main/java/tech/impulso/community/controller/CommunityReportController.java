package tech.impulso.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.community.dto.CommunityReportResponse;
import tech.impulso.community.dto.CreateReportRequest;
import tech.impulso.community.entity.ReportTargetType;
import tech.impulso.community.service.CommunityReportService;

/**
 * Controlador REST que expone los endpoints para que los usuarios
 * reporten contenido inapropiado en la comunidad (RF-034, moderación).
 */
@RestController
@RequestMapping("/api/community")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Reportes de comunidad",
        description = "Emisión de reportes sobre publicaciones o respuestas del foro.")
public class CommunityReportController {

    private final CommunityReportService service;

    public CommunityReportController(CommunityReportService service) {
        this.service = service;
    }

    /**
     * Reporta una publicación como contenido inapropiado.
     */
    @Operation(summary = "Reportar una publicación",
            description = "Registra un reporte sobre una publicación de la comunidad para revisión administrativa.")
    @PostMapping("/posts/{id}/reports")
    public ResponseEntity<CommunityReportResponse> reportPost(@PathVariable("id") Long id,
                                                              @Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createReport(ReportTargetType.POST, id, request));
    }

    /**
     * Reporta una respuesta como contenido inapropiado.
     */
    @Operation(summary = "Reportar una respuesta",
            description = "Registra un reporte sobre una respuesta de la comunidad para revisión administrativa.")
    @PostMapping("/replies/{id}/reports")
    public ResponseEntity<CommunityReportResponse> reportReply(@PathVariable("id") Long id,
                                                               @Valid @RequestBody CreateReportRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createReport(ReportTargetType.REPLY, id, request));
    }
}
