package tech.impulso.projects.controller;

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
import tech.impulso.projects.dto.CreateProjectRequest;
import tech.impulso.projects.dto.CreateSubmissionRequest;
import tech.impulso.projects.dto.ProjectResponse;
import tech.impulso.projects.dto.ProjectSubmissionResponse;
import tech.impulso.projects.dto.ReviewSubmissionRequest;
import tech.impulso.projects.dto.UpdateProjectRequest;
import tech.impulso.projects.entity.ProjectSubmissionStatus;
import tech.impulso.projects.service.ProjectService;

import java.util.List;

/**
 * Controlador REST para proyectos de programación y sus entregas
 * (RF-023 / RF-045).
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Proyectos",
        description = "Gestión de proyectos individuales y sus entregas.")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    // ------------------- Proyectos -------------------

    @Operation(summary = "Listar proyectos publicados")
    @GetMapping("/projects")
    public ResponseEntity<PagedResponse<ProjectResponse>> listPublished(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @RequestParam(value = "learningRouteId", required = false) Long learningRouteId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "moduleId", required = false) Long moduleId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listPublished(search, difficulty,
                learningRouteId, courseId, moduleId, pageable));
    }

    @Operation(summary = "Listar todos los proyectos (gestión)")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @GetMapping("/projects/manage")
    public ResponseEntity<PagedResponse<ProjectResponse>> listAll(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @RequestParam(value = "status", required = false) ContentStatus status,
            @RequestParam(value = "learningRouteId", required = false) Long learningRouteId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "moduleId", required = false) Long moduleId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listAll(search, difficulty, status,
                learningRouteId, courseId, moduleId, pageable));
    }

    @Operation(summary = "Consultar el detalle de un proyecto")
    @GetMapping("/projects/{id}")
    public ResponseEntity<ProjectResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Crear un proyecto")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping("/projects")
    public ResponseEntity<ProjectResponse> create(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Actualizar un proyecto")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/projects/{id}")
    public ResponseEntity<ProjectResponse> update(@PathVariable("id") Long id,
                                                  @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Cambiar el estado de un proyecto")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/projects/{id}/status")
    public ResponseEntity<ProjectResponse> changeStatus(@PathVariable("id") Long id,
                                                        @Valid @RequestBody UpdateContentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(id, request.status()));
    }

    @Operation(summary = "Eliminar un proyecto")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------- Entregas -------------------

    @Operation(summary = "Enviar una entrega al proyecto",
            description = "Registra una nueva versión de entrega para el usuario autenticado.")
    @PostMapping("/projects/{id}/submissions")
    public ResponseEntity<ProjectSubmissionResponse> submit(@PathVariable("id") Long id,
                                                            @Valid @RequestBody CreateSubmissionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(id, request));
    }

    @Operation(summary = "Listar mis entregas de un proyecto")
    @GetMapping("/projects/{id}/submissions/mine")
    public ResponseEntity<List<ProjectSubmissionResponse>> listMySubmissions(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.listMySubmissions(id));
    }

    @Operation(summary = "Listar entregas recibidas en un proyecto (gestión)")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @GetMapping("/projects/{id}/submissions")
    public ResponseEntity<PagedResponse<ProjectSubmissionResponse>> listProjectSubmissions(
            @PathVariable("id") Long id,
            @RequestParam(value = "status", required = false) ProjectSubmissionStatus status,
            @PageableDefault(size = 20, sort = "submittedAt") Pageable pageable) {
        return ResponseEntity.ok(service.listProjectSubmissions(id, status, pageable));
    }

    @Operation(summary = "Revisar una entrega",
            description = "Aplica el nuevo estado, la calificación y la retroalimentación a la entrega.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/project-submissions/{submissionId}/review")
    public ResponseEntity<ProjectSubmissionResponse> review(@PathVariable("submissionId") Long submissionId,
                                                            @Valid @RequestBody ReviewSubmissionRequest request) {
        return ResponseEntity.ok(service.review(submissionId, request));
    }
}
