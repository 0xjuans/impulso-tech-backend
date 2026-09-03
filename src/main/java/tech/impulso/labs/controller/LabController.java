package tech.impulso.labs.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.labs.dto.ExecuteCodeRequest;
import tech.impulso.labs.dto.ExecutionResultResponse;
import tech.impulso.labs.dto.LabRequest;
import tech.impulso.labs.dto.LabResponse;
import tech.impulso.labs.dto.LabSubmissionResponse;
import tech.impulso.labs.service.LabService;

import java.util.List;

/**
 * Controlador REST para laboratorios y sus entregas (RF-013, RF-037,
 * RF-038).
 *
 * <p>La ejecución de código se delega al sandbox aislado; este
 * controlador se limita a orquestar la petición y devolver el
 * resultado.</p>
 */
@RestController
@RequestMapping("/api/labs")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Laboratorios",
        description = "Gestión de laboratorios prácticos y sus entregas de código.")
public class LabController {

    private final LabService service;

    public LabController(LabService service) {
        this.service = service;
    }

    @Operation(summary = "Búsqueda paginada de laboratorios",
            description = "Los estudiantes sólo ven laboratorios publicados; los instructores ven los suyos "
                    + "en cualquier estado y los administradores ven todos.")
    @GetMapping
    public ResponseEntity<Page<LabResponse>> search(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(value = "language", required = false) String language,
            @RequestParam(value = "courseId", required = false) Long courseId,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.search(query, language, courseId, pageable));
    }

    @Operation(summary = "Consultar el detalle de un laboratorio")
    @GetMapping("/{id}")
    public ResponseEntity<LabResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @Operation(summary = "Crear un laboratorio",
            description = "El instructor autenticado queda como propietario del laboratorio.")
    @PostMapping
    public ResponseEntity<LabResponse> create(@Valid @RequestBody LabRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @Operation(summary = "Actualizar un laboratorio")
    @PutMapping("/{id}")
    public ResponseEntity<LabResponse> update(@PathVariable("id") Long id,
                                              @Valid @RequestBody LabRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @Operation(summary = "Eliminar un laboratorio")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Ejecutar código dentro del laboratorio",
            description = "Ejecuta el código en el sandbox sin persistir la entrega.")
    @PostMapping("/{id}/executions")
    public ResponseEntity<ExecutionResultResponse> tryExecute(@PathVariable("id") Long id,
                                                              @Valid @RequestBody ExecuteCodeRequest request) {
        return ResponseEntity.ok(service.tryExecute(id, request));
    }

    @Operation(summary = "Enviar una entrega",
            description = "Persiste el código enviado por el estudiante y el resultado de la ejecución.")
    @PostMapping("/{id}/submissions")
    public ResponseEntity<LabSubmissionResponse> submit(@PathVariable("id") Long id,
                                                        @Valid @RequestBody ExecuteCodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.submit(id, request));
    }

    @Operation(summary = "Listar mis entregas del laboratorio")
    @GetMapping("/{id}/submissions/mine")
    public ResponseEntity<List<LabSubmissionResponse>> listMySubmissions(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.listMySubmissions(id));
    }
}
