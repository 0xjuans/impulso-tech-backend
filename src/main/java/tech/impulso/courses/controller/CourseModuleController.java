package tech.impulso.courses.controller;

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
import tech.impulso.courses.dto.CourseModuleResponse;
import tech.impulso.courses.dto.CreateCourseModuleRequest;
import tech.impulso.courses.dto.UpdateCourseModuleRequest;
import tech.impulso.courses.service.CourseModuleService;

import java.util.List;

/**
 * Controlador REST para los módulos que componen un curso (RF-041).
 *
 * <p>Los endpoints se anidan bajo el curso propietario para reflejar la
 * relación jerárquica: los módulos no existen sin un curso al que
 * pertenecen.</p>
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Módulos de curso", description = "Operaciones sobre los módulos que componen un curso.")
public class CourseModuleController {

    private final CourseModuleService service;

    public CourseModuleController(CourseModuleService service) {
        this.service = service;
    }

    /**
     * Lista los módulos de un curso ordenados por su posición.
     */
    @Operation(summary = "Listar los módulos de un curso")
    @GetMapping("/courses/{courseId}/modules")
    public ResponseEntity<List<CourseModuleResponse>> listByCourse(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(service.listByCourse(courseId));
    }

    /**
     * Consulta el detalle de un módulo específico.
     */
    @Operation(summary = "Consultar el detalle de un módulo")
    @GetMapping("/course-modules/{moduleId}")
    public ResponseEntity<CourseModuleResponse> get(@PathVariable("moduleId") Long moduleId) {
        return ResponseEntity.ok(service.findById(moduleId));
    }

    /**
     * Crea un nuevo módulo dentro del curso indicado.
     */
    @Operation(summary = "Crear un módulo",
            description = "Crea el módulo en estado BORRADOR dentro del curso indicado.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping("/courses/{courseId}/modules")
    public ResponseEntity<CourseModuleResponse> create(@PathVariable("courseId") Long courseId,
                                                       @Valid @RequestBody CreateCourseModuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(courseId, request));
    }

    /**
     * Actualiza los campos permitidos de un módulo.
     */
    @Operation(summary = "Actualizar un módulo")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/course-modules/{moduleId}")
    public ResponseEntity<CourseModuleResponse> update(@PathVariable("moduleId") Long moduleId,
                                                       @Valid @RequestBody UpdateCourseModuleRequest request) {
        return ResponseEntity.ok(service.update(moduleId, request));
    }

    /**
     * Publica, deshabilita o vuelve a borrador un módulo.
     */
    @Operation(summary = "Cambiar el estado de un módulo")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/course-modules/{moduleId}/status")
    public ResponseEntity<CourseModuleResponse> changeStatus(@PathVariable("moduleId") Long moduleId,
                                                             @Valid @RequestBody UpdateContentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(moduleId, request.status()));
    }

    /**
     * Elimina un módulo y sus contenidos asociados.
     */
    @Operation(summary = "Eliminar un módulo",
            description = "Elimina el módulo del curso. Las lecciones y contenidos asociados se eliminan en cascada.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @DeleteMapping("/course-modules/{moduleId}")
    public ResponseEntity<Void> delete(@PathVariable("moduleId") Long moduleId) {
        service.delete(moduleId);
        return ResponseEntity.noContent().build();
    }
}
