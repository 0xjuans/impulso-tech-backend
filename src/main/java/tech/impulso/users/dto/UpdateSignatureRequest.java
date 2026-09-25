package tech.impulso.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Solicitud para actualizar la firma del instructor (RF-047).
 *
 * <p>La firma se envía como {@code data URL} de imagen PNG o JPEG. El
 * tamaño está limitado por dos motivos: evitar payloads abusivos y
 * mantener los tiempos de generación del PDF acotados.</p>
 *
 * @param signatureImage cadena {@code data:image/png;base64,...} o
 *                       {@code data:image/jpeg;base64,...}.
 */
public record UpdateSignatureRequest(
        @NotBlank(message = "La firma es obligatoria.")
        @Size(max = 350_000,
                message = "La firma es demasiado grande. Reduce la resolución o guárdala en PNG comprimido.")
        @Pattern(regexp = "^data:image/(png|jpeg);base64,[A-Za-z0-9+/=]+$",
                message = "La firma debe ser una imagen PNG o JPEG codificada en base64.")
        String signatureImage
) {
}
