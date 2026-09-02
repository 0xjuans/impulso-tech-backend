package tech.impulso.legal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.legal.dto.LegalAcceptanceResponse;
import tech.impulso.legal.dto.LegalDocumentResponse;
import tech.impulso.legal.entity.LegalDocumentType;
import tech.impulso.legal.service.LegalService;

import java.util.List;

/**
 * Controlador REST para consultar los documentos legales vigentes y
 * registrar aceptaciones del usuario autenticado (RF-062).
 */
@RestController
@RequestMapping("/api/legal")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Documentos legales",
        description = "Consulta de términos, política de privacidad y otros documentos, y gestión de aceptaciones.")
public class LegalController {

    private final LegalService service;

    public LegalController(LegalService service) {
        this.service = service;
    }

    @Operation(summary = "Listar los documentos legales vigentes")
    @GetMapping("/documents")
    public ResponseEntity<List<LegalDocumentResponse>> listCurrent() {
        return ResponseEntity.ok(service.listCurrent());
    }

    @Operation(summary = "Consultar la versión vigente de un tipo de documento")
    @GetMapping("/documents/{type}/current")
    public ResponseEntity<LegalDocumentResponse> getCurrent(@PathVariable("type") LegalDocumentType type) {
        return ResponseEntity.ok(service.getCurrent(type));
    }

    @Operation(summary = "Consultar el historial de versiones de un tipo de documento")
    @GetMapping("/documents/{type}/history")
    public ResponseEntity<List<LegalDocumentResponse>> listHistory(@PathVariable("type") LegalDocumentType type) {
        return ResponseEntity.ok(service.listHistory(type));
    }

    @Operation(summary = "Aceptar una versión de un documento legal",
            description = "Registra el consentimiento del usuario autenticado sobre la versión indicada.")
    @PostMapping("/documents/{id}/acceptances")
    public ResponseEntity<LegalAcceptanceResponse> accept(@PathVariable("id") Long documentId,
                                                          HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.accept(documentId, request));
    }

    @Operation(summary = "Listar mis aceptaciones")
    @GetMapping("/acceptances/mine")
    public ResponseEntity<List<LegalAcceptanceResponse>> listMyAcceptances() {
        return ResponseEntity.ok(service.listMyAcceptances());
    }

    @Operation(summary = "Listar los documentos vigentes que aún debo aceptar")
    @GetMapping("/acceptances/pending")
    public ResponseEntity<List<LegalDocumentResponse>> listMyPending() {
        return ResponseEntity.ok(service.listMyPending());
    }
}
