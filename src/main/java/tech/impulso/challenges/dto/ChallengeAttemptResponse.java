package tech.impulso.challenges.dto;

import tech.impulso.challenges.entity.ChallengeAttempt;
import tech.impulso.challenges.entity.ChallengeAttemptStatus;

import java.time.OffsetDateTime;

/**
 * Representación pública de un intento de reto.
 *
 * @param code Se incluye únicamente cuando el consumidor es el propio
 *             autor del intento o un instructor con permisos de revisión.
 */
public record ChallengeAttemptResponse(
        Long id,
        Long challengeId,
        String challengeName,
        Long userId,
        String userFullName,
        Integer attemptNumber,
        String language,
        String code,
        ChallengeAttemptStatus status,
        String feedback,
        Long reviewedById,
        OffsetDateTime reviewedAt,
        OffsetDateTime submittedAt,
        OffsetDateTime updatedAt
) {

    public static ChallengeAttemptResponse from(ChallengeAttempt attempt) {
        return new ChallengeAttemptResponse(
                attempt.getId(),
                attempt.getChallenge().getId(),
                attempt.getChallenge().getName(),
                attempt.getUser().getId(),
                "%s %s".formatted(attempt.getUser().getFirstName(), attempt.getUser().getLastName()),
                attempt.getAttemptNumber(),
                attempt.getLanguage(),
                attempt.getCode(),
                attempt.getStatus(),
                attempt.getFeedback(),
                attempt.getReviewedBy() == null ? null : attempt.getReviewedBy().getId(),
                attempt.getReviewedAt(),
                attempt.getSubmittedAt(),
                attempt.getUpdatedAt()
        );
    }
}
