package tech.impulso.projects.dto;

import tech.impulso.projects.entity.ProjectSubmission;
import tech.impulso.projects.entity.ProjectSubmissionStatus;

import java.time.OffsetDateTime;

/**
 * Representación pública de una entrega de proyecto.
 */
public record ProjectSubmissionResponse(
        Long id,
        Long projectId,
        String projectName,
        Long userId,
        String userFullName,
        Integer submissionNumber,
        String submissionUrl,
        String studentNotes,
        ProjectSubmissionStatus status,
        Integer grade,
        String feedback,
        boolean lateSubmission,
        Long reviewedById,
        OffsetDateTime reviewedAt,
        OffsetDateTime submittedAt,
        OffsetDateTime updatedAt
) {

    public static ProjectSubmissionResponse from(ProjectSubmission submission) {
        return new ProjectSubmissionResponse(
                submission.getId(),
                submission.getProject().getId(),
                submission.getProject().getName(),
                submission.getUser().getId(),
                "%s %s".formatted(submission.getUser().getFirstName(), submission.getUser().getLastName()),
                submission.getSubmissionNumber(),
                submission.getSubmissionUrl(),
                submission.getStudentNotes(),
                submission.getStatus(),
                submission.getGrade(),
                submission.getFeedback(),
                submission.isLateSubmission(),
                submission.getReviewedBy() == null ? null : submission.getReviewedBy().getId(),
                submission.getReviewedAt(),
                submission.getSubmittedAt(),
                submission.getUpdatedAt()
        );
    }
}
