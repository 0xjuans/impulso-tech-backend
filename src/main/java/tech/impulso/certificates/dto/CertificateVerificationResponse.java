package tech.impulso.certificates.dto;

import tech.impulso.certificates.entity.Certificate;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Vista pública de verificación de un certificado.
 *
 * <p>Se expone en el endpoint público de verificación por código y
 * contiene sólo la información necesaria para acreditar la validez del
 * certificado sin filtrar datos internos ni personales sensibles.</p>
 *
 * @param verificationCode código público del certificado.
 * @param recipientName    nombre visible del titular del certificado.
 * @param courseName       nombre del curso completado.
 * @param issuedAt         fecha de emisión (UTC).
 * @param valid            {@code true} cuando el certificado existe y
 *                         es válido.
 */
public record CertificateVerificationResponse(
        UUID verificationCode,
        String recipientName,
        String courseName,
        OffsetDateTime issuedAt,
        boolean valid
) {

    public static CertificateVerificationResponse from(Certificate certificate) {
        String first = certificate.getUser().getFirstName();
        String last = certificate.getUser().getLastName();
        String composed = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        String name = composed.isBlank() ? certificate.getUser().getUsername() : composed;
        return new CertificateVerificationResponse(
                certificate.getVerificationCode(),
                name,
                certificate.getCourse().getName(),
                certificate.getIssuedAt(),
                true
        );
    }
}
