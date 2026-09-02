package tech.impulso.legal.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.service.AdminActivityLogger;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.legal.dto.LegalAcceptanceResponse;
import tech.impulso.legal.dto.LegalDocumentResponse;
import tech.impulso.legal.dto.PublishLegalDocumentRequest;
import tech.impulso.legal.entity.LegalDocument;
import tech.impulso.legal.entity.LegalDocumentType;
import tech.impulso.legal.entity.UserLegalAcceptance;
import tech.impulso.legal.repository.LegalDocumentRepository;
import tech.impulso.legal.repository.UserLegalAcceptanceRepository;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Servicio con las operaciones sobre documentos legales y sus
 * aceptaciones (RF-062).
 *
 * <p>La publicación de una nueva versión retira automáticamente la
 * versión anterior del mismo tipo. La aceptación del usuario queda
 * registrada de forma idempotente por par (usuario, documento).</p>
 */
@Service
public class LegalService {

    /** Tipo utilizado en el registro de actividad administrativa. */
    private static final String TARGET_LEGAL = "LEGAL_DOCUMENT";

    /** Código de la acción registrada al publicar una nueva versión. */
    private static final String ACTION_PUBLISHED = "LEGAL_DOCUMENT_PUBLISHED";

    private final LegalDocumentRepository documentRepository;
    private final UserLegalAcceptanceRepository acceptanceRepository;
    private final CurrentUserService currentUserService;
    private final AdminActivityLogger activityLogger;

    public LegalService(LegalDocumentRepository documentRepository,
                        UserLegalAcceptanceRepository acceptanceRepository,
                        CurrentUserService currentUserService,
                        AdminActivityLogger activityLogger) {
        this.documentRepository = documentRepository;
        this.acceptanceRepository = acceptanceRepository;
        this.currentUserService = currentUserService;
        this.activityLogger = activityLogger;
    }

    /**
     * Devuelve la versión vigente del documento del tipo indicado.
     */
    @Transactional(readOnly = true)
    public LegalDocumentResponse getCurrent(LegalDocumentType type) {
        LegalDocument document = documentRepository.findCurrentByType(type)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "No existe una versión vigente para el tipo de documento indicado."));
        return LegalDocumentResponse.from(document);
    }

    /**
     * Devuelve todas las versiones vigentes de documentos legales.
     */
    @Transactional(readOnly = true)
    public List<LegalDocumentResponse> listCurrent() {
        return documentRepository.findAllCurrent().stream()
                .map(LegalDocumentResponse::from)
                .toList();
    }

    /**
     * Devuelve el historial de versiones publicadas de un tipo de
     * documento.
     */
    @Transactional(readOnly = true)
    public List<LegalDocumentResponse> listHistory(LegalDocumentType type) {
        return documentRepository.findByTypeOrderByPublishedAtDesc(type).stream()
                .map(LegalDocumentResponse::from)
                .toList();
    }

    /**
     * Publica una nueva versión de un documento legal. Retira la
     * versión anterior del mismo tipo (si existía) y registra la acción
     * administrativa.
     */
    @Transactional
    public LegalDocumentResponse publish(PublishLegalDocumentRequest request) {
        User admin = currentUserService.requireAuthenticatedUser();

        documentRepository.findCurrentByType(request.type()).ifPresent(previous -> {
            if (previous.getVersion().equals(request.version())) {
                throw new BusinessException(HttpStatus.CONFLICT,
                        "Ya existe una versión %s vigente para este tipo de documento."
                                .formatted(request.version()));
            }
            previous.setRetiredAt(OffsetDateTime.now(ZoneOffset.UTC));
        });

        LegalDocument document = new LegalDocument();
        document.setType(request.type());
        document.setVersion(request.version().trim());
        document.setTitle(request.title().trim());
        document.setContent(request.content());
        document.setRequiresAcceptance(request.requiresAcceptance());
        document.setPublishedBy(admin);
        document = documentRepository.save(document);

        activityLogger.log(
                admin,
                ACTION_PUBLISHED,
                TARGET_LEGAL,
                document.getId(),
                "%s versión %s publicada.".formatted(document.getType(), document.getVersion()));

        return LegalDocumentResponse.from(document);
    }

    /**
     * Registra la aceptación de un documento por parte del usuario
     * autenticado. La operación es idempotente: si el usuario ya lo
     * había aceptado devuelve la aceptación existente.
     */
    @Transactional
    public LegalAcceptanceResponse accept(Long documentId, HttpServletRequest request) {
        User user = currentUserService.requireAuthenticatedUser();
        LegalDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El documento indicado no existe."));

        if (document.getRetiredAt() != null) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "El documento indicado ya no es la versión vigente.");
        }

        if (acceptanceRepository.existsByUserIdAndDocumentId(user.getId(), documentId)) {
            UserLegalAcceptance existing = acceptanceRepository
                    .findByUserIdOrderByAcceptedAtDesc(user.getId()).stream()
                    .filter(a -> a.getDocument().getId().equals(documentId))
                    .findFirst()
                    .orElseThrow();
            return LegalAcceptanceResponse.from(existing);
        }

        UserLegalAcceptance acceptance = new UserLegalAcceptance();
        acceptance.setUser(user);
        acceptance.setDocument(document);
        acceptance.setIpAddress(request == null ? null : request.getRemoteAddr());
        return LegalAcceptanceResponse.from(acceptanceRepository.save(acceptance));
    }

    /**
     * Devuelve las aceptaciones registradas del usuario autenticado.
     */
    @Transactional(readOnly = true)
    public List<LegalAcceptanceResponse> listMyAcceptances() {
        User user = currentUserService.requireAuthenticatedUser();
        return acceptanceRepository.findByUserIdOrderByAcceptedAtDesc(user.getId()).stream()
                .map(LegalAcceptanceResponse::from)
                .toList();
    }

    /**
     * Devuelve las versiones vigentes que aún requieren aceptación por
     * parte del usuario autenticado.
     */
    @Transactional(readOnly = true)
    public List<LegalDocumentResponse> listMyPending() {
        User user = currentUserService.requireAuthenticatedUser();
        Set<Long> accepted = new HashSet<>(acceptanceRepository.findAcceptedDocumentIds(user.getId()));
        return documentRepository.findAllCurrent().stream()
                .filter(LegalDocument::isRequiresAcceptance)
                .filter(document -> !accepted.contains(document.getId()))
                .map(LegalDocumentResponse::from)
                .toList();
    }
}
