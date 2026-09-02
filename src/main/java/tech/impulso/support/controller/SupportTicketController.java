package tech.impulso.support.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.support.dto.CreateTicketRequest;
import tech.impulso.support.dto.SupportTicketResponse;
import tech.impulso.support.entity.SupportTicketStatus;
import tech.impulso.support.service.SupportTicketService;

/**
 * Controlador REST para las operaciones de soporte disponibles a
 * cualquier usuario autenticado (RF-036).
 */
@RestController
@RequestMapping("/api/support")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Soporte", description = "Creación y seguimiento de tickets de soporte del usuario autenticado.")
public class SupportTicketController {

    private final SupportTicketService service;

    public SupportTicketController(SupportTicketService service) {
        this.service = service;
    }

    /**
     * Crea un nuevo ticket de soporte a nombre del usuario autenticado.
     */
    @Operation(summary = "Crear un ticket de soporte")
    @PostMapping("/tickets")
    public ResponseEntity<SupportTicketResponse> create(@Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * Devuelve los tickets creados por el usuario autenticado.
     */
    @Operation(summary = "Listar mis tickets")
    @GetMapping("/tickets/mine")
    public ResponseEntity<PagedResponse<SupportTicketResponse>> listMine(
            @RequestParam(value = "status", required = false) SupportTicketStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listMine(status, pageable));
    }

    /**
     * Consulta el detalle de un ticket. Sólo accesible para el autor,
     * el responsable asignado o un administrador.
     */
    @Operation(summary = "Consultar el detalle de un ticket")
    @GetMapping("/tickets/{id}")
    public ResponseEntity<SupportTicketResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
