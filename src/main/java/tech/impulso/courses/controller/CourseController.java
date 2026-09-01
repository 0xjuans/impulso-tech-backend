package tech.impulso.courses.controller;

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
import tech.impulso.common.content.dto.UpdateContentStatusRequest;
import tech.impulso.courses.dto.CourseResponse;
import tech.impulso.courses.dto.CreateCourseRequest;
import tech.impulso.courses.dto.UpdateCourseRequest;
import tech.impulso.courses.service.CourseService;

/**
 * Controlador REST para los cursos (RF-040).
 */
@RestController
@RequestMapping("/api/courses")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cursos", description = "Operaciones sobre cursos.")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    /**
     * Lista los cursos publicados disponibles para todos los usuarios
     * autenticados.
     */
    @Operation(summary = "Listar cursos publicados",
            description = "Devuelve los cursos publicados y disponibles para los estudiantes.")
    @GetMapping
    public ResponseEntity<PagedResponse<CourseResponse>> listPublished(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @RequestParam(value = "learningRouteId", required = false) Long learningRouteId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listPublished(search, difficulty, learningRouteId, pageable));
    }

    /**
     * Lista todos los cursos incluyendo borradores y deshabilitados.
     * Reservado para instructores y administradores.
     */
    @Operation(summary = "Listar todos los cursos (gestión)")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @GetMapping("/manage")
    public ResponseEntity<PagedResponse<CourseResponse>> listAll(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @RequestParam(value = "status", required = false) ContentStatus status,
            @RequestParam(value = "learningRouteId", required = false) Long learningRouteId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listAll(search, difficulty, status, learningRouteId, pageable));
    }

    /**
     * Consulta el detalle de un curso.
     */
    @Operation(summary = "Consultar el detalle de un curso")
    @GetMapping("/{id}")
    public ResponseEntity<CourseResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /**
     * Crea un nuevo curso.
     */
    @Operation(summary = "Crear un curso",
            description = "Crea el curso en estado BORRADOR y asigna al usuario autenticado como instructor responsable.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CreateCourseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    /**
     * Actualiza los campos permitidos de un curso existente.
     */
    @Operation(summary = "Actualizar un curso")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/{id}")
    public ResponseEntity<CourseResponse> update(@PathVariable("id") Long id,
                                                 @Valid @RequestBody UpdateCourseRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    /**
     * Publica, deshabilita o vuelve a borrador el curso indicado.
     */
    @Operation(summary = "Cambiar el estado de un curso")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<CourseResponse> changeStatus(@PathVariable("id") Long id,
                                                       @Valid @RequestBody UpdateContentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(id, request.status()));
    }
}
