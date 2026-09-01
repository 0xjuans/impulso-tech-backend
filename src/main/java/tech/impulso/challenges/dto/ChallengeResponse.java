package tech.impulso.challenges.dto;

import tech.impulso.challenges.entity.Challenge;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;

import java.time.OffsetDateTime;

/**
 * Representación pública de un reto de programación.
 *
 * <p>El campo {@code hiddenTestCases} solo se incluye cuando el
 * consumidor puede gestionar el reto (instructor dueño o administrador);
 * al estudiante se le entrega {@code null} para no filtrar las pruebas
 * ocultas utilizadas en la calificación.</p>
 */
public record ChallengeResponse(
        Long id,
        String name,
        String description,
        String objective,
        String instructions,
        DifficultyLevel difficulty,
        String allowedLanguages,
        String ioExamples,
        String restrictions,
        String publicTestCases,
        String hiddenTestCases,
        int xpReward,
        Integer estimatedMinutes,
        ContentStatus status,
        Long learningRouteId,
        Long courseId,
        Long moduleId,
        Long lessonId,
        Long instructorId,
        String instructorFullName,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación pública a partir de la entidad.
     *
     * @param challenge     entidad a convertir.
     * @param forInstructor cuando es {@code true} se incluyen los
     *                      casos de prueba ocultos.
     * @return DTO listo para devolver desde la API.
     */
    public static ChallengeResponse from(Challenge challenge, boolean forInstructor) {
        return new ChallengeResponse(
                challenge.getId(),
                challenge.getName(),
                challenge.getDescription(),
                challenge.getObjective(),
                challenge.getInstructions(),
                challenge.getDifficulty(),
                challenge.getAllowedLanguages(),
                challenge.getIoExamples(),
                challenge.getRestrictions(),
                challenge.getPublicTestCases(),
                forInstructor ? challenge.getHiddenTestCases() : null,
                challenge.getXpReward(),
                challenge.getEstimatedMinutes(),
                challenge.getStatus(),
                challenge.getLearningRoute() == null ? null : challenge.getLearningRoute().getId(),
                challenge.getCourse() == null ? null : challenge.getCourse().getId(),
                challenge.getModule() == null ? null : challenge.getModule().getId(),
                challenge.getLesson() == null ? null : challenge.getLesson().getId(),
                challenge.getInstructor().getId(),
                "%s %s".formatted(challenge.getInstructor().getFirstName(), challenge.getInstructor().getLastName()),
                challenge.getCreatedAt(),
                challenge.getUpdatedAt()
        );
    }
}
