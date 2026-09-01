package tech.impulso.resources.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.common.content.dto.UpdateContentStatusRequest;
import tech.impulso.resources.dto.CreateResourceRequest;
import tech.impulso.resources.dto.EducationalResourceResponse;
import tech.impulso.resources.dto.UpdateResourceRequest;
import tech.impulso.resources.entity.ResourceType;
import tech.impulso.resources.service.EducationalResourceService;

/**
 * Controlador REST de la biblioteca de recursos educativos (RF-024).
 */
@RestController
@RequestMapping("/api/resources")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recursos educativos",
        description = "Consulta y gestión de la biblioteca de recursos educativos de Impulso Tech.")
public class EducationalResourceController {

    private final EducationalResourceService service;

    public EducationalResourceController(EducationalResourceService service) {
        this.service = service;
    }

    /**
     * Lista los recursos publicados disponibles para todos los usuarios
     * autenticados.
     */
    @Operation(summary = "Listar recursos publicados",
            description = "Devuelve los recursos publicados con soporte de búsqueda y filtros.")
    @GetMapping
    public ResponseEntity<PagedResponse<EducationalResourceResponse>> listPublished(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "type", required = false) ResourceType type,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @RequestParam(value = "learningRouteId", required = false) Long learningRouteId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "moduleId", required = false) Long moduleId,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listPublished(search, type, difficulty,
                learningRouteId, courseId, moduleId, lessonId, pageable));
    }

    /**
     * Lista todos los recursos incluyendo borradores y deshabilitados.
     * Reservado para instructores y administradores.
     */
    @Operation(summary = "Listar todos los recursos (gestión)")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @GetMapping("/manage")
    public ResponseEntity<PagedResponse<EducationalResourceResponse>> listAll(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "type", required = false) ResourceType type,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @RequestParam(value = "status", required = false) ContentStatus status,
            @RequestParam(value = "learningRouteId", required = false) Long learningRouteId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "moduleId", required = false) Long moduleId,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listAll(search, type, difficulty, status,
                learningRouteId, courseId, moduleId, lessonId, pageable));
    }

    /**
     * Consulta el detalle de un recurso.
     */
    @Operation(summary = "Consultar el detalle de un recurso")
    @GetMapping("/{id}")
    public ResponseEntity<EducationalResourceResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Crea un nuevo recurso educativo.
     */
    @Operation(summary = "Crear un recurso",
            description = "Crea el recurso en estado BORRADOR y asigna al usuario autenticado como creador.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<EducationalResourceResponse> create(@Valid @RequestBody CreateResourceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * Actualiza los campos permitidos de un recurso existente.
     */
    @Operation(summary = "Actualizar un recurso")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<EducationalResourceResponse> update(@PathVariable("id") Long id,
                                                              @Valid @RequestBody UpdateResourceRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Publica, deshabilita o vuelve a borrador un recurso.
     */
    @Operation(summary = "Cambiar el estado de un recurso")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<EducationalResourceResponse> changeStatus(@PathVariable("id") Long id,
                                                                    @Valid @RequestBody UpdateContentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(id, request.status()));
    }

    /**
     * Elimina un recurso.
     */
    @Operation(summary = "Eliminar un recurso")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
