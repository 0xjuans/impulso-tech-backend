package tech.impulso.challenges.controller;

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
import tech.impulso.challenges.dto.ChallengeAttemptResponse;
import tech.impulso.challenges.dto.ChallengeResponse;
import tech.impulso.challenges.dto.CreateChallengeRequest;
import tech.impulso.challenges.dto.ReviewChallengeAttemptRequest;
import tech.impulso.challenges.dto.SubmitChallengeAttemptRequest;
import tech.impulso.challenges.dto.UpdateChallengeRequest;
import tech.impulso.challenges.entity.ChallengeAttemptStatus;
import tech.impulso.challenges.service.ChallengeService;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.common.content.dto.UpdateContentStatusRequest;

import java.util.List;

/**
 * Controlador REST para retos de programación y sus intentos
 * (RF-014).
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Retos", description = "Gestión y resolución de retos de programación.")
public class ChallengeController {

    private final ChallengeService service;

    public ChallengeController(ChallengeService service) {
        this.service = service;
    }

    // ------------------- Retos -------------------

    @Operation(summary = "Listar retos publicados")
    @GetMapping("/challenges")
    public ResponseEntity<PagedResponse<ChallengeResponse>> listPublished(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "learningRouteId", required = false) Long learningRouteId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "moduleId", required = false) Long moduleId,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listPublished(search, difficulty, language,
                learningRouteId, courseId, moduleId, lessonId, pageable));
    }

    @Operation(summary = "Listar todos los retos (gestión)")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @GetMapping("/challenges/manage")
    public ResponseEntity<PagedResponse<ChallengeResponse>> listAll(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "difficulty", required = false) DifficultyLevel difficulty,
            @RequestParam(value = "status", required = false) ContentStatus status,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "learningRouteId", required = false) Long learningRouteId,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @RequestParam(value = "moduleId", required = false) Long moduleId,
            @RequestParam(value = "lessonId", required = false) Long lessonId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listAll(search, difficulty, status, language,
                learningRouteId, courseId, moduleId, lessonId, pageable));
    }

    @Operation(summary = "Consultar el detalle de un reto")
    @GetMapping("/challenges/{id}")
    public ResponseEntity<ChallengeResponse> get(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @Operation(summary = "Crear un reto")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping("/challenges")
    public ResponseEntity<ChallengeResponse> create(@Valid @RequestBody CreateChallengeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Actualizar un reto")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/challenges/{id}")
    public ResponseEntity<ChallengeResponse> update(@PathVariable("id") Long id,
                                                    @Valid @RequestBody UpdateChallengeRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Cambiar el estado de un reto")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/challenges/{id}/status")
    public ResponseEntity<ChallengeResponse> changeStatus(@PathVariable("id") Long id,
                                                          @Valid @RequestBody UpdateContentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(id, request.status()));
    }

    @Operation(summary = "Eliminar un reto")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @DeleteMapping("/challenges/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------- Intentos -------------------

    @Operation(summary = "Enviar un intento al reto",
            description = "Registra un nuevo intento del estudiante. El intento queda pendiente de revisión.")
    @PostMapping("/challenges/{id}/attempts")
    public ResponseEntity<ChallengeAttemptResponse> submit(@PathVariable("id") Long id,
                                                           @Valid @RequestBody SubmitChallengeAttemptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(id, request));
    }

    @Operation(summary = "Listar mis intentos de un reto")
    @GetMapping("/challenges/{id}/attempts/mine")
    public ResponseEntity<List<ChallengeAttemptResponse>> listMyAttempts(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.listMyAttempts(id));
    }

    @Operation(summary = "Listar intentos recibidos en un reto (gestión)")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @GetMapping("/challenges/{id}/attempts")
    public ResponseEntity<PagedResponse<ChallengeAttemptResponse>> listChallengeAttempts(
            @PathVariable("id") Long id,
            @RequestParam(value = "status", required = false) ChallengeAttemptStatus status,
            @PageableDefault(size = 20, sort = "submittedAt") Pageable pageable) {
        return ResponseEntity.ok(service.listChallengeAttempts(id, status, pageable));
    }

    @Operation(summary = "Revisar un intento de reto",
            description = "Aplica el nuevo estado y la retroalimentación al intento.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/challenge-attempts/{attemptId}/review")
    public ResponseEntity<ChallengeAttemptResponse> review(@PathVariable("attemptId") Long attemptId,
                                                           @Valid @RequestBody ReviewChallengeAttemptRequest request) {
        return ResponseEntity.ok(service.review(attemptId, request));
    }
}
