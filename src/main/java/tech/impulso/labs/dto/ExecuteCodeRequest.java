package tech.impulso.labs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para ejecutar código dentro del contexto de un laboratorio
 * (RF-037, RF-038).
 *
 * @param code  código a ejecutar. Se aplica un límite superior para
 *              acotar la carga enviada al sandbox.
 * @param stdin entrada estándar opcional pasada al proceso.
 */
public record ExecuteCodeRequest(
        @NotBlank @Size(max = 20_000) String code,
        @Size(max = 4_000) String stdin
) {
}
