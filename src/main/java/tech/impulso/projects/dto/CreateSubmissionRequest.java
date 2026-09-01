package tech.impulso.projects.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Datos requeridos para que un estudiante envíe una nueva entrega de
 * un proyecto.
 *
 * @param submissionUrl URL con el trabajo entregado (repositorio,
 *                      documento o archivo).
 * @param studentNotes  notas o comentarios opcionales del estudiante.
 */
public record CreateSubmissionRequest(
        @NotBlank(message = "La URL de la entrega es obligatoria.")
        @Size(max = 1000)
        String submissionUrl,

        String studentNotes
) {
}
