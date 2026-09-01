package tech.impulso.projects.dto;

import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.projects.entity.Project;

import java.time.OffsetDateTime;

/**
 * Representación pública de un proyecto.
 */
public record ProjectResponse(
        Long id,
        String name,
        String description,
        String objective,
        String instructions,
        String requirements,
        DifficultyLevel difficulty,
        String technologies,
        String resources,
        String evaluationCriteria,
        int maxScore,
        int xpReward,
        OffsetDateTime deadlineAt,
        ContentStatus status,
        Long learningRouteId,
        Long courseId,
        Long moduleId,
        Long instructorId,
        String instructorFullName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getObjective(),
                project.getInstructions(),
                project.getRequirements(),
                project.getDifficulty(),
                project.getTechnologies(),
                project.getResources(),
                project.getEvaluationCriteria(),
                project.getMaxScore(),
                project.getXpReward(),
                project.getDeadlineAt(),
                project.getStatus(),
                project.getLearningRoute() == null ? null : project.getLearningRoute().getId(),
                project.getCourse() == null ? null : project.getCourse().getId(),
                project.getModule() == null ? null : project.getModule().getId(),
                project.getInstructor().getId(),
                "%s %s".formatted(project.getInstructor().getFirstName(), project.getInstructor().getLastName()),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
