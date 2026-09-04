package tech.impulso.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.messaging.dto.ConversationResponse;
import tech.impulso.messaging.dto.MessageResponse;
import tech.impulso.messaging.dto.SendMessageRequest;
import tech.impulso.messaging.dto.StartConversationRequest;
import tech.impulso.messaging.service.MessagingService;

/**
 * Controlador REST de mensajería directa entre usuarios (RF-061).
 *
 * <p>Ofrece los endpoints necesarios para listar conversaciones, abrir
 * una nueva con otro usuario, consultar mensajes, enviarlos y marcar
 * la conversación como leída.</p>
 */
@RestController
@RequestMapping("/api/messages")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Mensajería",
        description = "Conversaciones y mensajes directos entre usuarios de la plataforma.")
public class MessagingController {

    private final MessagingService service;

    public MessagingController(MessagingService service) {
        this.service = service;
    }

    @Operation(summary = "Listar mis conversaciones",
            description = "Devuelve las conversaciones del usuario autenticado ordenadas por el último mensaje.")
    @GetMapping("/conversations")
    public ResponseEntity<Page<ConversationResponse>> listMyConversations(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.listMyConversations(pageable));
    }

    @Operation(summary = "Abrir o recuperar una conversación con otro usuario",
            description = "La operación es idempotente: si ya existe una conversación con el usuario indicado se devuelve la misma.")
    @PostMapping("/conversations")
    public ResponseEntity<ConversationResponse> openConversation(@Valid @RequestBody StartConversationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.openConversation(request.recipientId()));
    }

    @Operation(summary = "Listar mensajes de una conversación",
            description = "Devuelve los mensajes ordenados del más reciente al más antiguo.")
    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<Page<MessageResponse>> listMessages(@PathVariable("id") Long id,
                                                              @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.listMessages(id, pageable));
    }

    @Operation(summary = "Enviar un mensaje en la conversación")
    @PostMapping("/conversations/{id}/messages")
    public ResponseEntity<MessageResponse> sendMessage(@PathVariable("id") Long id,
                                                       @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.sendMessage(id, request));
    }

    @Operation(summary = "Marcar la conversación como leída",
            description = "Marca como leídos todos los mensajes cuyo emisor no sea el usuario autenticado.")
    @PutMapping("/conversations/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable("id") Long id) {
        service.markAsRead(id);
        return ResponseEntity.noContent().build();
    }
}
