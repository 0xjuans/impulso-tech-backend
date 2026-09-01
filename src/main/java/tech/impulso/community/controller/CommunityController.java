package tech.impulso.community.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import tech.impulso.community.dto.CommunityPostDetailResponse;
import tech.impulso.community.dto.CommunityPostResponse;
import tech.impulso.community.dto.CommunityReplyResponse;
import tech.impulso.community.dto.CreatePostRequest;
import tech.impulso.community.dto.CreateReplyRequest;
import tech.impulso.community.dto.UpdatePostRequest;
import tech.impulso.community.dto.UpdateReplyRequest;
import tech.impulso.community.entity.RelatedContentType;
import tech.impulso.community.service.CommunityService;

/**
 * Controlador REST de la comunidad y foro de aprendizaje (RF-034).
 */
@RestController
@RequestMapping("/api/community")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Comunidad",
        description = "Publicaciones, respuestas, votos y respuesta aceptada del foro de aprendizaje.")
public class CommunityController {

    private final CommunityService service;

    public CommunityController(CommunityService service) {
        this.service = service;
    }

    /**
     * Lista las publicaciones de la comunidad.
     */
    @Operation(summary = "Listar publicaciones")
    @GetMapping("/posts")
    public ResponseEntity<PagedResponse<CommunityPostResponse>> listPosts(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "relatedType", required = false) RelatedContentType relatedType,
            @RequestParam(value = "relatedId", required = false) Long relatedId,
            @RequestParam(value = "authorId", required = false) Long authorId,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(service.listPosts(search, relatedType, relatedId, authorId, pageable));
    }

    /**
     * Devuelve el detalle de una publicación con todas sus respuestas.
     */
    @Operation(summary = "Consultar el detalle de una publicación")
    @GetMapping("/posts/{id}")
    public ResponseEntity<CommunityPostDetailResponse> getPost(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getPost(id));
    }

    /**
     * Crea una nueva publicación.
     */
    @Operation(summary = "Crear una publicación")
    @PostMapping("/posts")
    public ResponseEntity<CommunityPostResponse> createPost(@Valid @RequestBody CreatePostRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPost(request));
    }

    /**
     * Actualiza una publicación existente.
     */
    @Operation(summary = "Actualizar una publicación")
    @PatchMapping("/posts/{id}")
    public ResponseEntity<CommunityPostResponse> updatePost(@PathVariable("id") Long id,
                                                            @Valid @RequestBody UpdatePostRequest request) {
        return ResponseEntity.ok(service.updatePost(id, request));
    }

    /**
     * Elimina una publicación.
     */
    @Operation(summary = "Eliminar una publicación")
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable("id") Long id) {
        service.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Publica una respuesta en la publicación indicada.
     */
    @Operation(summary = "Responder a una publicación")
    @PostMapping("/posts/{id}/replies")
    public ResponseEntity<CommunityReplyResponse> createReply(@PathVariable("id") Long id,
                                                              @Valid @RequestBody CreateReplyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createReply(id, request));
    }

    /**
     * Actualiza una respuesta existente.
     */
    @Operation(summary = "Actualizar una respuesta")
    @PatchMapping("/replies/{id}")
    public ResponseEntity<CommunityReplyResponse> updateReply(@PathVariable("id") Long id,
                                                              @Valid @RequestBody UpdateReplyRequest request) {
        return ResponseEntity.ok(service.updateReply(id, request));
    }

    /**
     * Elimina una respuesta.
     */
    @Operation(summary = "Eliminar una respuesta")
    @DeleteMapping("/replies/{id}")
    public ResponseEntity<Void> deleteReply(@PathVariable("id") Long id) {
        service.deleteReply(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Alterna la marca de "útil" del usuario autenticado sobre la
     * respuesta indicada.
     */
    @Operation(summary = "Alternar marca de útil sobre una respuesta",
            description = "Si el usuario aún no la había marcado la registra; si ya la había marcado la retira.")
    @PostMapping("/replies/{id}/helpful")
    public ResponseEntity<CommunityReplyResponse> toggleHelpful(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.toggleHelpful(id));
    }

    /**
     * Marca una respuesta como aceptada.
     */
    @Operation(summary = "Marcar la respuesta aceptada de una publicación",
            description = "Reservado al autor de la publicación o a un administrador.")
    @PostMapping("/posts/{postId}/accepted-reply/{replyId}")
    public ResponseEntity<CommunityPostResponse> acceptReply(@PathVariable("postId") Long postId,
                                                             @PathVariable("replyId") Long replyId) {
        return ResponseEntity.ok(service.setAcceptedReply(postId, replyId));
    }

    /**
     * Quita la marca de respuesta aceptada.
     */
    @Operation(summary = "Quitar la respuesta aceptada de una publicación")
    @DeleteMapping("/posts/{postId}/accepted-reply")
    public ResponseEntity<CommunityPostResponse> clearAcceptedReply(@PathVariable("postId") Long postId) {
        return ResponseEntity.ok(service.setAcceptedReply(postId, null));
    }
}
