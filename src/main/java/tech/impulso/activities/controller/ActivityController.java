package tech.impulso.activities.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.activities.dto.ActivityResponse;
import tech.impulso.activities.dto.AttemptResultResponse;
import tech.impulso.activities.dto.CreateActivityRequest;
import tech.impulso.activities.dto.SubmitAttemptRequest;
import tech.impulso.activities.dto.UpdateActivityRequest;
import tech.impulso.activities.service.ActivityService;
import tech.impulso.common.content.dto.UpdateContentStatusRequest;

import java.util.List;

/**
 * Controlador REST para las actividades de una lección (RF-042).
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Actividades",
        description = "Gestión y resolución de actividades auto-corregibles asociadas a una lección.")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    /**
     * Lista las actividades de una lección ordenadas por posición.
     */
    @Operation(summary = "Listar las actividades de una lección")
    @GetMapping("/lessons/{lessonId}/activities")
    public ResponseEntity<List<ActivityResponse>> listByLesson(@PathVariable("lessonId") Long lessonId) {
        return ResponseEntity.ok(service.listByLesson(lessonId));
    }

    /**
     * Consulta el detalle de una actividad.
     */
    @Operation(summary = "Consultar el detalle de una actividad")
    @GetMapping("/activities/{activityId}")
    public ResponseEntity<ActivityResponse> get(@PathVariable("activityId") Long activityId) {
        return ResponseEntity.ok(service.findById(activityId));
    }

    /**
     * Crea una nueva actividad dentro de la lección indicada.
     */
    @Operation(summary = "Crear una actividad",
            description = "Crea la actividad en estado BORRADOR dentro de la lección indicada.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping("/lessons/{lessonId}/activities")
    public ResponseEntity<ActivityResponse> create(@PathVariable("lessonId") Long lessonId,
                                                   @Valid @RequestBody CreateActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(lessonId, request));
    }

    /**
     * Actualiza los campos permitidos de una actividad existente.
     */
    @Operation(summary = "Actualizar una actividad")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/activities/{activityId}")
    public ResponseEntity<ActivityResponse> update(@PathVariable("activityId") Long activityId,
                                                   @Valid @RequestBody UpdateActivityRequest request) {
        return ResponseEntity.ok(service.update(activityId, request));
    }

    /**
     * Publica, deshabilita o vuelve a borrador una actividad.
     */
    @Operation(summary = "Cambiar el estado de una actividad")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/activities/{activityId}/status")
    public ResponseEntity<ActivityResponse> changeStatus(@PathVariable("activityId") Long activityId,
                                                         @Valid @RequestBody UpdateContentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(activityId, request.status()));
    }

    /**
     * Elimina una actividad y sus intentos asociados.
     */
    @Operation(summary = "Eliminar una actividad")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @DeleteMapping("/activities/{activityId}")
    public ResponseEntity<Void> delete(@PathVariable("activityId") Long activityId) {
        service.delete(activityId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Envía un intento del estudiante sobre una actividad y devuelve el
     * resultado evaluado.
     */
    @Operation(summary = "Enviar un intento",
            description = "Registra la respuesta del estudiante, evalúa la actividad y devuelve el resultado.")
    @PostMapping("/activities/{activityId}/attempts")
    public ResponseEntity<AttemptResultResponse> submitAttempt(@PathVariable("activityId") Long activityId,
                                                               @Valid @RequestBody SubmitAttemptRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submitAttempt(activityId, request));
    }
}
