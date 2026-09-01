package tech.impulso.activities.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Respuesta enviada por el estudiante para intentar resolver una
 * actividad.
 *
 * <p>El contenido depende del tipo de la actividad, por lo que se
 * transmite en formato JSON. El servicio lo interpreta según el tipo
 * configurado en la actividad correspondiente.</p>
 *
 * @param answer respuesta del estudiante en formato JSON.
 */
public record SubmitAttemptRequest(
        @NotBlank(message = "La respuesta es obligatoria.")
        String answer
) {
}
