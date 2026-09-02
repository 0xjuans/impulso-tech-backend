package tech.impulso.legal.dto;

import tech.impulso.legal.entity.LegalDocument;
import tech.impulso.legal.entity.LegalDocumentType;

import java.time.OffsetDateTime;

/**
 * Representación pública de una versión de documento legal.
 */
public record LegalDocumentResponse(
        Long id,
        LegalDocumentType type,
        String version,
        String title,
        String content,
        boolean requiresAcceptance,
        boolean current,
        OffsetDateTime publishedAt,
        OffsetDateTime retiredAt
) {

    public static LegalDocumentResponse from(LegalDocument document) {
        return new LegalDocumentResponse(
                document.getId(),
                document.getType(),
                document.getVersion(),
                document.getTitle(),
                document.getContent(),
                document.isRequiresAcceptance(),
                document.getRetiredAt() == null,
                document.getPublishedAt(),
                document.getRetiredAt()
        );
    }
}
