package tech.impulso.certificates.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.certificates.dto.CertificateResponse;
import tech.impulso.certificates.dto.CertificateVerificationResponse;
import tech.impulso.certificates.entity.Certificate;
import tech.impulso.certificates.repository.CertificateRepository;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.courses.entity.Course;
import tech.impulso.users.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio con la lógica de negocio de los certificados (RF-047).
 *
 * <p>Es la fuente de verdad para la emisión, consulta y verificación
 * pública de certificados. La emisión es idempotente: si ya existe un
 * certificado para el par (estudiante, curso) se devuelve el existente
 * sin generar uno nuevo.</p>
 */
@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final CertificatePdfRenderer pdfRenderer;
    private final String verificationBaseUrl;

    public CertificateService(CertificateRepository certificateRepository,
                              CertificatePdfRenderer pdfRenderer,
                              @Value("${app.certificates.verification-base-url}")
                              String verificationBaseUrl) {
        this.certificateRepository = certificateRepository;
        this.pdfRenderer = pdfRenderer;
        this.verificationBaseUrl = verificationBaseUrl;
    }

    /**
     * Emite un certificado para la combinación estudiante/curso indicada,
     * salvo que el curso no lo genere o ya exista uno emitido.
     *
     * @return el certificado (nuevo o existente), o {@link Optional#empty()}
     *         cuando el curso no genera certificado.
     */
    @Transactional
    public Optional<Certificate> issueIfEligible(User user, Course course) {
        if (course == null || !course.isGeneratesCertificate()) {
            return Optional.empty();
        }
        if (certificateRepository.existsByUserIdAndCourseId(user.getId(), course.getId())) {
            return certificateRepository.findByUserIdOrderByIssuedAtDesc(user.getId()).stream()
                    .filter(c -> c.getCourse().getId().equals(course.getId()))
                    .findFirst();
        }
        Certificate certificate = new Certificate();
        certificate.setUser(user);
        certificate.setCourse(course);
        return Optional.of(certificateRepository.save(certificate));
    }

    /**
     * Certificados otorgados al usuario indicado.
     */
    @Transactional(readOnly = true)
    public List<CertificateResponse> listForUser(Long userId) {
        return certificateRepository.findByUserIdOrderByIssuedAtDesc(userId).stream()
                .map(CertificateResponse::from)
                .toList();
    }

    /**
     * Verificación pública por código: devuelve los datos mínimos para
     * acreditar el certificado ante terceros.
     */
    @Transactional(readOnly = true)
    public CertificateVerificationResponse verify(UUID code) {
        Certificate certificate = requireByCode(code);
        return CertificateVerificationResponse.from(certificate);
    }

    /**
     * Genera el PDF del certificado en el momento de la descarga. No se
     * persiste ningún archivo: cada solicitud reconstruye el documento a
     * partir de los datos actuales.
     */
    @Transactional(readOnly = true)
    public GeneratedPdf renderPdf(UUID code) {
        Certificate certificate = requireByCode(code);
        String recipient = recipientName(certificate.getUser());
        String verificationUrl = verificationBaseUrl + "/" + certificate.getVerificationCode();
        byte[] bytes = pdfRenderer.render(certificate, verificationUrl, recipient);
        String filename = "certificado-" + certificate.getVerificationCode() + ".pdf";
        return new GeneratedPdf(bytes, filename);
    }

    private Certificate requireByCode(UUID code) {
        return certificateRepository.findByVerificationCode(code)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "No existe un certificado con el código indicado."));
    }

    private static String recipientName(User user) {
        String first = user.getFirstName();
        String last = user.getLastName();
        String composed = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        return composed.isBlank() ? user.getUsername() : composed;
    }

    /**
     * Contenedor sencillo del PDF generado y su nombre de archivo
     * sugerido para el header {@code Content-Disposition}.
     */
    public record GeneratedPdf(byte[] bytes, String filename) {
    }
}
