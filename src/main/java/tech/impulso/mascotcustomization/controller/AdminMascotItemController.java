package tech.impulso.mascotcustomization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.mascotcustomization.dto.MascotItemRequest;
import tech.impulso.mascotcustomization.entity.MascotItem;
import tech.impulso.mascotcustomization.repository.MascotItemRepository;
import tech.impulso.mascotcustomization.service.MascotCustomizationService;

import java.util.List;

/**
 * Controlador REST administrativo del catálogo de ítems de la
 * mascota (RF-022, RF-050, RF-052).
 */
@RestController
@RequestMapping("/api/admin/mascot/items")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Administración del catálogo de la mascota",
        description = "Gestión del catálogo de ítems desbloqueables de la mascota.")
public class AdminMascotItemController {

    private final MascotCustomizationService service;
    private final MascotItemRepository itemRepository;

    public AdminMascotItemController(MascotCustomizationService service,
                                     MascotItemRepository itemRepository) {
        this.service = service;
        this.itemRepository = itemRepository;
    }

    @Operation(summary = "Listar todos los ítems del catálogo")
    @GetMapping
    public ResponseEntity<List<MascotItem>> list() {
        return ResponseEntity.ok(itemRepository.findAll());
    }

    @Operation(summary = "Crear un ítem")
    @PostMapping
    public ResponseEntity<MascotItem> create(@Valid @RequestBody MascotItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createItem(request));
    }

    @Operation(summary = "Actualizar un ítem")
    @PutMapping("/{id}")
    public ResponseEntity<MascotItem> update(@PathVariable("id") Long id,
                                             @Valid @RequestBody MascotItemRequest request) {
        return ResponseEntity.ok(service.updateItem(id, request));
    }

    @Operation(summary = "Eliminar un ítem")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
