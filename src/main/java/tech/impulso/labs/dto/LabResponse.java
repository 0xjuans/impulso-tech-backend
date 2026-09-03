package tech.impulso.labs.dto;

import tech.impulso.common.content.ContentStatus;
import tech.impulso.labs.entity.Lab;

import java.time.OffsetDateTime;

/**
 * Representación pública de un laboratorio (RF-013).
 *
 * <p>La salida esperada ({@code expectedOutput}) sólo se expone al
 * instructor propietario y al administrador; los estudiantes reciben
 * este campo como {@code null} para evitar revelar respuestas.</p>
 */
public record LabResponse(
        Long id,
        String title,
        String description,
        String instructions,
        String language,
        String starterCode,
        String expectedOutput,
        int executionTimeoutMs,
        Long courseId,
        Long lessonId,
        Long instructorId,
        ContentStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public static LabResponse forOwner(Lab lab) {
        return new LabResponse(
                lab.getId(),
                lab.getTitle(),
                lab.getDescription(),
                lab.getInstructions(),
                lab.getLanguage(),
                lab.getStarterCode(),
                lab.getExpectedOutput(),
                lab.getExecutionTimeoutMs(),
                lab.getCourse() == null ? null : lab.getCourse().getId(),
                lab.getLesson() == null ? null : lab.getLesson().getId(),
                lab.getInstructor().getId(),
                lab.getStatus(),
                lab.getCreatedAt(),
                lab.getUpdatedAt()
        );
    }

    public static LabResponse forStudent(Lab lab) {
        return new LabResponse(
                lab.getId(),
                lab.getTitle(),
                lab.getDescription(),
                lab.getInstructions(),
                lab.getLanguage(),
                lab.getStarterCode(),
                null,
                lab.getExecutionTimeoutMs(),
                lab.getCourse() == null ? null : lab.getCourse().getId(),
                lab.getLesson() == null ? null : lab.getLesson().getId(),
                lab.getInstructor().getId(),
                lab.getStatus(),
                lab.getCreatedAt(),
                lab.getUpdatedAt()
        );
    }
}
