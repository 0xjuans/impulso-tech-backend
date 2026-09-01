package tech.impulso.enrollments.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.enrollments.dto.CourseProgressResponse;
import tech.impulso.enrollments.dto.EnrollmentResponse;
import tech.impulso.enrollments.service.EnrollmentService;

/**
 * Controlador REST para la inscripción de estudiantes a cursos y el
 * seguimiento del progreso (RF-011).
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Inscripciones y progreso",
        description = "Inscripción a cursos, marcado de lecciones completadas y consulta del progreso del estudiante.")
public class EnrollmentController {

    private final EnrollmentService service;

    public EnrollmentController(EnrollmentService service) {
        this.service = service;
    }

    /**
     * Inscribe al usuario autenticado en el curso indicado.
     */
    @Operation(summary = "Inscribirse en un curso",
            description = "Inscribe al usuario autenticado en el curso. La operación es idempotente.")
    @PostMapping("/courses/{courseId}/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.enroll(courseId));
    }

    /**
     * Lista las inscripciones del usuario autenticado.
     */
    @Operation(summary = "Listar mis inscripciones",
            description = "Devuelve las inscripciones a cursos del usuario autenticado.")
    @GetMapping("/users/me/enrollments")
    public ResponseEntity<PagedResponse<EnrollmentResponse>> listMine(
            @PageableDefault(size = 20, sort = "lastAccessedAt") Pageable pageable) {
        return ResponseEntity.ok(service.listMyEnrollments(pageable));
    }

    /**
     * Marca una lección como completada por el usuario autenticado.
     */
    @Operation(summary = "Marcar una lección como completada",
            description = "Registra que el usuario autenticado completó la lección. Devuelve el progreso actualizado del curso.")
    @PostMapping("/lessons/{lessonId}/complete")
    public ResponseEntity<CourseProgressResponse> completeLesson(@PathVariable("lessonId") Long lessonId) {
        return ResponseEntity.ok(service.completeLesson(lessonId));
    }

    /**
     * Consulta el progreso del usuario autenticado en un curso.
     */
    @Operation(summary = "Consultar el progreso en un curso",
            description = "Devuelve el porcentaje de avance, las lecciones obligatorias completadas y el estado de la inscripción.")
    @GetMapping("/users/me/courses/{courseId}/progress")
    public ResponseEntity<CourseProgressResponse> getProgress(@PathVariable("courseId") Long courseId) {
        return ResponseEntity.ok(service.getProgress(courseId));
    }
}
