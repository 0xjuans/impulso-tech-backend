package tech.impulso.legal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.legal.dto.LegalDocumentResponse;
import tech.impulso.legal.dto.PublishLegalDocumentRequest;
import tech.impulso.legal.service.LegalService;

/**
 * Controlador REST administrativo para publicar nuevas versiones de
 * documentos legales (RF-062).
 */
@RestController
@RequestMapping("/api/admin/legal/documents")
@PreAuthorize("hasRole('ADMINISTRADOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Gestión de documentos legales",
        description = "Publicación de nuevas versiones de términos, política de privacidad y demás documentos.")
public class AdminLegalController {

    private final LegalService service;

    public AdminLegalController(LegalService service) {
        this.service = service;
    }

    @Operation(summary = "Publicar una nueva versión de un documento legal",
            description = "Retira automáticamente la versión anterior del mismo tipo y registra la acción administrativa.")
    @PostMapping
    public ResponseEntity<LegalDocumentResponse> publish(@Valid @RequestBody PublishLegalDocumentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.publish(request));
    }
}
