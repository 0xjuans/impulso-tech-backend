package tech.impulso.legal.dto;

import tech.impulso.legal.entity.UserLegalAcceptance;

import java.time.OffsetDateTime;

/**
 * Representación pública de una aceptación de documento legal.
 */
public record LegalAcceptanceResponse(
        Long id,
        Long documentId,
        String documentType,
        String documentVersion,
        String documentTitle,
        OffsetDateTime acceptedAt
) {

    public static LegalAcceptanceResponse from(UserLegalAcceptance acceptance) {
        return new LegalAcceptanceResponse(
                acceptance.getId(),
                acceptance.getDocument().getId(),
                acceptance.getDocument().getType().name(),
                acceptance.getDocument().getVersion(),
                acceptance.getDocument().getTitle(),
                acceptance.getAcceptedAt()
        );
    }
}
