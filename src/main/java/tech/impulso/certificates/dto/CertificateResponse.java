package tech.impulso.certificates.dto;

import tech.impulso.certificates.entity.Certificate;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Vista completa de un certificado, destinada al propio estudiante.
 *
 * @param id              identificador interno del certificado.
 * @param verificationCode código público usado para verificar la validez.
 * @param courseId        curso al que corresponde.
 * @param courseName      nombre del curso.
 * @param recipientName   nombre completo del estudiante.
 * @param issuedAt        fecha de emisión (UTC).
 */
public record CertificateResponse(
        Long id,
        UUID verificationCode,
        Long courseId,
        String courseName,
        String recipientName,
        OffsetDateTime issuedAt
) {

    public static CertificateResponse from(Certificate certificate) {
        return new CertificateResponse(
                certificate.getId(),
                certificate.getVerificationCode(),
                certificate.getCourse().getId(),
                certificate.getCourse().getName(),
                fullName(certificate),
                certificate.getIssuedAt()
        );
    }

    private static String fullName(Certificate certificate) {
        String first = certificate.getUser().getFirstName();
        String last = certificate.getUser().getLastName();
        String composed = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        return composed.isBlank() ? certificate.getUser().getUsername() : composed;
    }
}
