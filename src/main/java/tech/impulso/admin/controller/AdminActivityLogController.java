package tech.impulso.admin.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.AdminActivityLogResponse;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.admin.entity.AdminActivityLog;
import tech.impulso.admin.repository.AdminActivityLogRepository;

/**
 * Controlador REST para consultar el registro de actividad
 * administrativa (RF-056).
 *
 * <p>Solo los administradores pueden consultar este registro. La
 * escritura ocurre automáticamente en las operaciones administrativas
 * relevantes; el registro es de solo lectura desde la API.</p>
 */
@RestController
@RequestMapping("/api/admin/activity")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Actividad administrativa",
        description = "Consulta del registro de acciones administrativas ejecutadas en la plataforma.")
public class AdminActivityLogController {

    private final AdminActivityLogRepository repository;

    public AdminActivityLogController(AdminActivityLogRepository repository) {
        this.repository = repository;
    }

    /**
     * Devuelve una página con las entradas del registro que coinciden
     * con los filtros suministrados.
     *
     * @param adminId    identificador del administrador responsable (opcional).
     * @param action     código de la acción (opcional).
     * @param targetType tipo del recurso afectado (opcional).
     * @param pageable   configuración de paginación y ordenamiento.
     * @return página con las entradas coincidentes.
     */
    @Operation(summary = "Consultar el registro de actividad administrativa",
            description = "Devuelve las acciones sensibles registradas en la plataforma con soporte de filtros y paginación.")
    @GetMapping
    public ResponseEntity<PagedResponse<AdminActivityLogResponse>> list(
            @RequestParam(value = "adminId", required = false) Long adminId,
            @RequestParam(value = "action", required = false) String action,
            @RequestParam(value = "targetType", required = false) String targetType,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        Page<AdminActivityLog> page = repository.search(adminId, action, targetType, pageable);
        return ResponseEntity.ok(PagedResponse.from(page, AdminActivityLogResponse::from));
    }
}
