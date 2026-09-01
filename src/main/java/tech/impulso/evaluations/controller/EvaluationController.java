package tech.impulso.evaluations.controller;

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
import tech.impulso.evaluations.dto.CreateEvaluationQuestionRequest;
import tech.impulso.evaluations.dto.CreateEvaluationRequest;
import tech.impulso.evaluations.dto.EvaluationAttemptResponse;
import tech.impulso.evaluations.dto.EvaluationResponse;
import tech.impulso.evaluations.dto.SubmitEvaluationRequest;
import tech.impulso.evaluations.dto.UpdateEvaluationRequest;
import tech.impulso.evaluations.service.EvaluationService;

import java.util.List;

/**
 * Controlador REST para las evaluaciones (RF-015 / RF-043).
 */
@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Evaluaciones",
        description = "Gestión y resolución de evaluaciones auto-corregibles asociadas a una lección.")
public class EvaluationController {

    private final EvaluationService service;

    public EvaluationController(EvaluationService service) {
        this.service = service;
    }

    /**
     * Lista las evaluaciones de una lección.
     */
    @Operation(summary = "Listar las evaluaciones de una lección")
    @GetMapping("/lessons/{lessonId}/evaluations")
    public ResponseEntity<List<EvaluationResponse>> listByLesson(@PathVariable("lessonId") Long lessonId) {
        return ResponseEntity.ok(service.listByLesson(lessonId));
    }

    /**
     * Consulta el detalle de una evaluación.
     */
    @Operation(summary = "Consultar el detalle de una evaluación")
    @GetMapping("/evaluations/{evaluationId}")
    public ResponseEntity<EvaluationResponse> get(@PathVariable("evaluationId") Long evaluationId) {
        return ResponseEntity.ok(service.findById(evaluationId));
    }

    /**
     * Crea una nueva evaluación dentro de la lección indicada.
     */
    @Operation(summary = "Crear una evaluación",
            description = "Crea la evaluación en estado BORRADOR dentro de la lección indicada. Las preguntas se agregan por separado.")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping("/lessons/{lessonId}/evaluations")
    public ResponseEntity<EvaluationResponse> create(@PathVariable("lessonId") Long lessonId,
                                                     @Valid @RequestBody CreateEvaluationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(lessonId, request));
    }

    /**
     * Actualiza los campos permitidos de una evaluación existente.
     */
    @Operation(summary = "Actualizar una evaluación")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/evaluations/{evaluationId}")
    public ResponseEntity<EvaluationResponse> update(@PathVariable("evaluationId") Long evaluationId,
                                                     @Valid @RequestBody UpdateEvaluationRequest request) {
        return ResponseEntity.ok(service.update(evaluationId, request));
    }

    /**
     * Publica, deshabilita o vuelve a borrador una evaluación.
     */
    @Operation(summary = "Cambiar el estado de una evaluación")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PatchMapping("/evaluations/{evaluationId}/status")
    public ResponseEntity<EvaluationResponse> changeStatus(@PathVariable("evaluationId") Long evaluationId,
                                                           @Valid @RequestBody UpdateContentStatusRequest request) {
        return ResponseEntity.ok(service.changeStatus(evaluationId, request.status()));
    }

    /**
     * Elimina una evaluación y sus preguntas e intentos asociados.
     */
    @Operation(summary = "Eliminar una evaluación")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @DeleteMapping("/evaluations/{evaluationId}")
    public ResponseEntity<Void> delete(@PathVariable("evaluationId") Long evaluationId) {
        service.delete(evaluationId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Agrega una pregunta a la evaluación indicada.
     */
    @Operation(summary = "Agregar una pregunta a una evaluación")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @PostMapping("/evaluations/{evaluationId}/questions")
    public ResponseEntity<EvaluationResponse> addQuestion(@PathVariable("evaluationId") Long evaluationId,
                                                          @Valid @RequestBody CreateEvaluationQuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addQuestion(evaluationId, request));
    }

    /**
     * Elimina una pregunta específica de una evaluación.
     */
    @Operation(summary = "Eliminar una pregunta de una evaluación")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMINISTRADOR')")
    @DeleteMapping("/evaluation-questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable("questionId") Long questionId) {
        service.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Inicia un intento del estudiante autenticado sobre la evaluación.
     */
    @Operation(summary = "Iniciar un intento",
            description = "Crea o recupera el intento en curso del usuario autenticado sobre la evaluación.")
    @PostMapping("/evaluations/{evaluationId}/attempts")
    public ResponseEntity<EvaluationAttemptResponse> startAttempt(@PathVariable("evaluationId") Long evaluationId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.startAttempt(evaluationId));
    }

    /**
     * Envía las respuestas y finaliza el intento en curso.
     */
    @Operation(summary = "Enviar el intento en curso",
            description = "Envía las respuestas del intento actual y calcula el resultado.")
    @PostMapping("/evaluations/{evaluationId}/attempts/submit")
    public ResponseEntity<EvaluationAttemptResponse> submitAttempt(@PathVariable("evaluationId") Long evaluationId,
                                                                   @Valid @RequestBody SubmitEvaluationRequest request) {
        return ResponseEntity.ok(service.submitAttempt(evaluationId, request));
    }

    /**
     * Devuelve el historial de intentos finalizados del usuario
     * autenticado sobre la evaluación.
     */
    @Operation(summary = "Listar mis intentos",
            description = "Devuelve los intentos finalizados del usuario autenticado sobre la evaluación.")
    @GetMapping("/evaluations/{evaluationId}/attempts")
    public ResponseEntity<List<EvaluationAttemptResponse>> listMyAttempts(
            @PathVariable("evaluationId") Long evaluationId) {
        return ResponseEntity.ok(service.listMyAttempts(evaluationId));
    }
}
