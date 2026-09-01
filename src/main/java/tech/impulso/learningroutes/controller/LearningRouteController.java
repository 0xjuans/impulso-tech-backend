package tech.impulso.learningroutes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import tech.impulso.learningroutes.dto.CreateLearningRouteRequest;
import tech.impulso.learningroutes.dto.LearningRouteResponse;
import tech.impulso.learningroutes.dto.UpdateContentStatusRequest;
import tech.impulso.learningroutes.dto.UpdateLearningRouteRequest;
import tech.impulso.learningroutes.service.LearningRouteService;

/**
 * Controlador REST para las rutas de aprendizaje (RF-039).
 *
 * <p>Expone tanto los endpoints de consulta disponibles para cualquier
 * usuario autenticado como los endpoints de gestión reservados a
 * instructores y administradores. La autorización específica se aplica
 * a nivel de método mediante {@link PreAuthorize} y, para las
 * operaciones sobre una ruta existente, mediante validaciones adicionales
 * en el servicio (comprobación de propiedad).</p>
 */
@RestController
@RequestMapping("/api/learning-routes")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Rutas de aprendizaje", description = "Operaciones sobre rutas de aprendizaje.")
public class LearningRouteController {

    private final LearningRouteService service;

    public LearningRouteController(LearningRouteService service) {
        this.service = service;
    }

    /**
     * Lista las rutas de aprendizaje publicadas, disponibles para todos
     * los usuarios autenticados.
     */
    @Operation(summary = "Listar rutas publicadas",
            description = "Devuelve las rutas de aprendizaje publicadas y disponibles para los estudiantes.")
    @GetMapping
    public ResponseEntity<PagedResponse<LearningRouteResponse>> listPublished(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listPublished(search, difficulty, pageable));
    }

    /**
     * Lista todas las rutas incluyendo borradores y deshabilitadas. Sólo
     * disponible para instructores y administradores.
     */
    @Operation(summary = "Listar todas las rutas (gestión)",
            description = "Devuelve las rutas en cualquier estado. Reservado para instructores y administradores.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @GetMapping("/manage")
    public ResponseEntity<PagedResponse<LearningRouteResponse>> listAll(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @RequestParam(value = "status", required = false) ContentStatus status,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listAll(search, difficulty, status, pageable));
    }

    /**
     * Consulta el detalle de una ruta.
     */
    @Operation(summary = "Consultar el detalle de una ruta")
    @GetMapping("/{id}")
    public ResponseEntity<LearningRouteResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Crea una nueva ruta de aprendizaje.
     */
    @Operation(summary = "Crear una ruta de aprendizaje",
            description = "Crea la ruta en estado BORRADOR y asigna al usuario autenticado como instructor responsable.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<LearningRouteResponse> create(@Valid @RequestBody CreateLearningRouteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * Actualiza los campos permitidos de una ruta existente.
     */
    @Operation(summary = "Actualizar una ruta de aprendizaje")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<LearningRouteResponse> update(@PathVariable("id") Long id,
                                                         @Valid @RequestBody UpdateLearningRouteRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Publica, deshabilita o vuelve a borrador una ruta existente.
     */
    @Operation(summary = "Cambiar el estado de una ruta",
            description = "Publica, deshabilita o regresa a borrador la ruta indicada.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<LearningRouteResponse> changeStatus(@PathVariable("id") Long id,
                                                              @Valid @RequestBody UpdateContentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(id, request.status()));
    }
}
