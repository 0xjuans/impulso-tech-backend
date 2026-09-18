package tech.impulso.certificates.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.certificates.dto.CertificateResponse;
import tech.impulso.certificates.dto.CertificateVerificationResponse;
import tech.impulso.certificates.service.CertificateService;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.users.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints de certificados (RF-047).
 *
 * <p>Ofrece tres puntos de acceso:</p>
 * <ul>
 *   <li>{@code GET /api/users/me/certificates}: listado privado.</li>
 *   <li>{@code GET /api/certificates/{code}}: verificación pública.</li>
 *   <li>{@code GET /api/certificates/{code}/download}: descarga en PDF.</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Certificados", description = "Emisión, listado y verificación pública de certificados.")
public class CertificateController {

    private final CertificateService certificateService;
    private final CurrentUserService currentUserService;

    public CertificateController(CertificateService certificateService,
                                 CurrentUserService currentUserService) {
        this.certificateService = certificateService;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve los certificados del usuario autenticado.
     */
    @Operation(summary = "Listar mis certificados",
            description = "Devuelve los certificados obtenidos por el usuario autenticado.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/users/me/certificates")
    public ResponseEntity<List<CertificateResponse>> listMine() {
        User user = currentUserService.requireAuthenticatedUser();
        return ResponseEntity.ok(certificateService.listForUser(user.getId()));
    }

    /**
     * Verifica públicamente un certificado por su código.
     */
    @Operation(summary = "Verificar certificado",
            description = "Verifica públicamente la validez de un certificado por su código público.")
    @GetMapping("/certificates/{code}")
    public ResponseEntity<CertificateVerificationResponse> verify(@PathVariable UUID code) {
        return ResponseEntity.ok(certificateService.verify(code));
    }

    /**
     * Descarga pública del certificado en PDF, generado on-the-fly.
     */
    @Operation(summary = "Descargar certificado en PDF",
            description = "Descarga el certificado como PDF a partir de su código público.")
    @GetMapping("/certificates/{code}/download")
    public ResponseEntity<byte[]> download(@PathVariable UUID code) {
        CertificateService.GeneratedPdf pdf = certificateService.renderPdf(code);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + pdf.filename() + "\"")
                .body(pdf.bytes());
    }
}
