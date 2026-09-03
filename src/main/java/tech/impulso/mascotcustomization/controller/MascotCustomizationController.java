package tech.impulso.mascotcustomization.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.mascotcustomization.dto.CatalogItemResponse;
import tech.impulso.mascotcustomization.dto.CustomizationResponse;
import tech.impulso.mascotcustomization.entity.MascotItemSlot;
import tech.impulso.mascotcustomization.service.MascotCustomizationService;

import java.util.List;

/**
 * Controlador REST para el catálogo y la personalización de la
 * mascota del estudiante autenticado (RF-022, RF-050, RF-052).
 */
@RestController
@RequestMapping("/api/mascot")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Personalización de la mascota",
        description = "Catálogo de ítems desbloqueables y personalización activa del estudiante.")
public class MascotCustomizationController {

    private final MascotCustomizationService service;

    public MascotCustomizationController(MascotCustomizationService service) {
        this.service = service;
    }

    @Operation(summary = "Consultar el catálogo",
            description = "Devuelve el catálogo con el estado (desbloqueado / equipado) del usuario autenticado.")
    @GetMapping("/catalog")
    public ResponseEntity<List<CatalogItemResponse>> catalog() {
        return ResponseEntity.ok(service.catalog());
    }

    @Operation(summary = "Consultar mi personalización actual")
    @GetMapping("/customization")
    public ResponseEntity<CustomizationResponse> myCustomization() {
        return ResponseEntity.ok(service.myCustomization());
    }

    @Operation(summary = "Equipar un ítem")
    @PutMapping("/customization/items/{itemId}")
    public ResponseEntity<CustomizationResponse> equip(@PathVariable("itemId") Long itemId) {
        return ResponseEntity.ok(service.equip(itemId));
    }

    @Operation(summary = "Retirar el ítem de una ranura",
            description = "Deja la ranura indicada con el aspecto por defecto.")
    @DeleteMapping("/customization/slots/{slot}")
    public ResponseEntity<CustomizationResponse> unequip(@PathVariable("slot") MascotItemSlot slot) {
        return ResponseEntity.ok(service.unequip(slot));
    }
}
