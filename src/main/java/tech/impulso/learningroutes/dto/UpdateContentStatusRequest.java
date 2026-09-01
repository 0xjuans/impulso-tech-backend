package tech.impulso.learningroutes.dto;

import jakarta.validation.constraints.NotNull;
import tech.impulso.common.content.ContentStatus;

/**
 * Solicitud para modificar el estado del ciclo de vida de un contenido
 * educativo (ruta, curso, módulo, lección).
 *
 * @param status nuevo estado que se aplicará al contenido.
 */
public record UpdateContentStatusRequest(
        @NotNull(message = "El estado es obligatorio.")
        ContentStatus status
) {
}
