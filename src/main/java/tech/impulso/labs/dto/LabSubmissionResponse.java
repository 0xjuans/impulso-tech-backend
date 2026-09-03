package tech.impulso.labs.dto;

import tech.impulso.labs.entity.LabSubmission;

import java.time.OffsetDateTime;

/**
 * Representación pública de una entrega de laboratorio (RF-038).
 *
 * @param id               identificador de la entrega.
 * @param labId            laboratorio al que corresponde.
 * @param submissionNumber número consecutivo de la entrega.
 * @param code             código enviado.
 * @param stdout           salida estándar capturada.
 * @param stderr           salida de error capturada.
 * @param exitCode         código de salida del proceso.
 * @param executionTimeMs  duración de la ejecución.
 * @param passed           indica si la entrega se considera aprobada.
 * @param submittedAt      fecha de la entrega.
 */
public record LabSubmissionResponse(
        Long id,
        Long labId,
        int submissionNumber,
        String code,
        String stdout,
        String stderr,
        Integer exitCode,
        Integer executionTimeMs,
        boolean passed,
        OffsetDateTime submittedAt
) {

    public static LabSubmissionResponse from(LabSubmission submission) {
        return new LabSubmissionResponse(
                submission.getId(),
                submission.getLab().getId(),
                submission.getSubmissionNumber(),
                submission.getCode(),
                submission.getStdout(),
                submission.getStderr(),
                submission.getExitCode(),
                submission.getExecutionTimeMs(),
                submission.isPassed(),
                submission.getSubmittedAt()
        );
    }
}
