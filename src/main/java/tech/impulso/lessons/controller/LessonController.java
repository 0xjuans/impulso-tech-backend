package tech.impulso.lessons.controller;

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
import tech.impulso.common.content.dto.UpdateContentStatusRequest;
import tech.impulso.lessons.dto.CreateLessonRequest;
import tech.impulso.lessons.dto.LessonResponse;
import tech.impulso.lessons.dto.UpdateLessonRequest;
import tech.impulso.lessons.service.LessonService;

import java.util.List;

/**
 * Controlador REST para las lecciones de un módulo (RF-041).
 *
 * <p>El listado se anida bajo el módulo propietario; las operaciones
 * sobre una lección concreta utilizan un recurso raíz para simplificar
 * la construcción de rutas por parte del cliente.</p>
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Lecciones", description = "Operaciones sobre las lecciones de un módulo.")
public class LessonController {

    private final LessonService service;

    public LessonController(LessonService service) {
        this.service = service;
    }

    /**
     * Lista las lecciones de un módulo ordenadas por su posición.
     */
    @Operation(summary = "Listar las lecciones de un módulo")
    @GetMapping("/course-modules/{moduleId}/lessons")
    public ResponseEntity<List<LessonResponse>> listByModule(@PathVariable("moduleId") Long moduleId) {
        return ResponseEntity.ok(service.listByModule(moduleId));
    }

    /**
     * Consulta el detalle de una lección específica.
     */
    @Operation(summary = "Consultar el detalle de una lección")
    @GetMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> get(@PathVariable("lessonId") Long lessonId) {
        return ResponseEntity.ok(service.findById(lessonId));
    }

    /**
     * Crea una nueva lección dentro del módulo indicado.
     */
    @Operation(summary = "Crear una lección",
            description = "Crea la lección en estado BORRADOR dentro del módulo indicado.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping("/course-modules/{moduleId}/lessons")
    public ResponseEntity<LessonResponse> create(@PathVariable("moduleId") Long moduleId,
                                                 @Valid @RequestBody CreateLessonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(moduleId, request));
    }

    /**
     * Actualiza los campos permitidos de una lección.
     */
    @Operation(summary = "Actualizar una lección")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/lessons/{lessonId}")
    public ResponseEntity<LessonResponse> update(@PathVariable("lessonId") Long lessonId,
                                                 @Valid @RequestBody UpdateLessonRequest request) {
        return ResponseEntity.ok(service.update(lessonId, request));
    }

    /**
     * Publica, deshabilita o vuelve a borrador una lección.
     */
    @Operation(summary = "Cambiar el estado de una lección")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/lessons/{lessonId}/status")
    public ResponseEntity<LessonResponse> changeStatus(@PathVariable("lessonId") Long lessonId,
                                                       @Valid @RequestBody UpdateContentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(lessonId, request.status()));
    }

    /**
     * Elimina una lección del módulo.
     */
    @Operation(summary = "Eliminar una lección")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @DeleteMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> delete(@PathVariable("lessonId") Long lessonId) {
        service.delete(lessonId);
        return ResponseEntity.noContent().build();
    }
}
