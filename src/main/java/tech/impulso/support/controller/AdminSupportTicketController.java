package tech.impulso.support.controller;

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
import tech.impulso.support.dto.SupportTicketResponse;
import tech.impulso.support.dto.UpdateTicketRequest;
import tech.impulso.support.entity.SupportTicketStatus;
import tech.impulso.support.entity.SupportTicketType;
import tech.impulso.support.service.SupportTicketService;

/**
 * Controlador REST para la gestión administrativa de tickets de
 * soporte (RF-036). El instructor sólo verá los tickets que tenga
 * asignados; el administrador tiene visibilidad completa.
 */
@RestController
@RequestMapping("/api/admin/support/tickets")
@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Gestión de soporte",
        description = "Panel para instructores y administradores encargados de resolver tickets.")
public class AdminSupportTicketController {

    private final SupportTicketService service;

    public AdminSupportTicketController(SupportTicketService service) {
        this.service = service;
    }

    /**
     * Lista los tickets aplicando los filtros indicados. Los
     * administradores pueden filtrar por responsable arbitrario; los
     * instructores sólo ven los tickets que les fueron asignados.
     */
    @Operation(summary = "Listar tickets",
            description = "Devuelve los tickets con soporte de filtros. El instructor sólo ve los tickets que tiene asignados.")
    @GetMapping
    public ResponseEntity<PagedResponse<SupportTicketResponse>> list(
            @RequestParam(value = "status", required = false) SupportTicketStatus status,
            @RequestParam(value = "type", required = false) SupportTicketType type,
            @RequestParam(value = "assigneeId", required = false) Long assigneeId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listForStaff(status, type, assigneeId, pageable));
    }

    /**
     * Actualiza el estado, la asignación o las notas de resolución de
     * un ticket.
     */
    @Operation(summary = "Actualizar un ticket")
    @PatchMapping("/{id}")
    public ResponseEntity<SupportTicketResponse> update(@PathVariable("id") Long id,
                                                        @Valid @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }
}
