package tech.impulso.ai.controller;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.ai.dto.ConversationResponse;
import tech.impulso.ai.dto.MessageResponse;
import tech.impulso.ai.dto.SendMessageRequest;
import tech.impulso.ai.dto.StartConversationRequest;
import tech.impulso.ai.service.MascotService;

/**
 * Controlador REST para la mascota IA de Impulso Tech (RF-017,
 * RF-051).
 *
 * <p>Expone la creación de conversaciones, el envío de mensajes y la
 * consulta del historial del usuario autenticado. Toda la interacción
 * con el proveedor externo ocurre en el servidor; el frontend jamás se
 * comunica de forma directa con la API de la IA (RF-027).</p>
 */
@RestController
@RequestMapping("/api/ai/mascot")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Mascota IA",
        description = "Conversaciones con la mascota educativa de Impulso Tech.")
public class MascotController {

    private final MascotService service;

    public MascotController(MascotService service) {
        this.service = service;
    }

    @Operation(summary = "Iniciar una nueva conversación")
    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> start(@Valid @RequestBody StartConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.start(request));
    }

    @Operation(summary = "Listar mis conversaciones",
            description = "Devuelve las conversaciones del usuario autenticado, más recientes primero.")
    @GetMapping("/conversations")
    public ResponseEntity<Page<ConversationResponse>> listMine(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.listMine(pageable));
    }

    @Operation(summary = "Consultar el detalle de una conversación",
            description = "Incluye los mensajes visibles al usuario en orden cronológico.")
    @GetMapping("/conversations/{id}")
    public ResponseEntity<ConversationResponse> getDetail(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getDetail(id));
    }

    @Operation(summary = "Enviar un mensaje a la mascota",
            description = "Envía el mensaje del usuario y devuelve la respuesta generada por la IA.")
    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable("id") Long id,
                                                       @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.sendMessage(id, request));
    }

    @Operation(summary = "Cerrar una conversación",
            description = "Marca la conversación como cerrada; el historial se conserva.")
    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> close(@PathVariable("id") Long id) {
        service.close(id);
        return ResponseEntity.noContent().build();
    }
}
