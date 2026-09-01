package tech.impulso.resources.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.resources.dto.FavoriteResourceResponse;
import tech.impulso.resources.service.ResourceFavoriteService;

/**
 * Controlador REST para la gestión de recursos favoritos del usuario
 * autenticado (RF-025).
 */
@RestController
@RequestMapping("/api/users/me/favorites/resources")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recursos favoritos",
        description = "Lista personal de recursos educativos marcados como favoritos por el usuario.")
public class ResourceFavoriteController {

    private final ResourceFavoriteService service;

    public ResourceFavoriteController(ResourceFavoriteService service) {
        this.service = service;
    }

    /**
     * Devuelve la lista de recursos favoritos del usuario autenticado.
     */
    @Operation(summary = "Listar mis favoritos",
            description = "Devuelve los recursos que el usuario autenticado ha marcado como favoritos.")
    @GetMapping
    public ResponseEntity<PagedResponse<FavoriteResourceResponse>> listMine(
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listMine(pageable));
    }

    /**
     * Marca el recurso indicado como favorito. Idempotente.
     */
    @Operation(summary = "Agregar un recurso a favoritos")
    @PostMapping("/{resourceId}")
    public ResponseEntity<FavoriteResourceResponse> add(@PathVariable("resourceId") Long resourceId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(resourceId));
    }

    /**
     * Quita el recurso indicado de la lista de favoritos.
     */
    @Operation(summary = "Quitar un recurso de favoritos")
    @DeleteMapping("/{resourceId}")
    public ResponseEntity<Void> remove(@PathVariable("resourceId") Long resourceId) {
        service.remove(resourceId);
        return ResponseEntity.noContent().build();
    }
}
