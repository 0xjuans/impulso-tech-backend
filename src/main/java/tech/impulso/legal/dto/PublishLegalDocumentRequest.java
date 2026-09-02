package tech.impulso.legal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tech.impulso.legal.entity.LegalDocumentType;

/**
 * Solicitud del administrador para publicar una nueva versión de un
 * documento legal. Al publicar la nueva versión se retira
 * automáticamente la versión anterior del mismo tipo.
 *
 * @param type               tipo del documento legal.
 * @param version            identificador de versión (por ejemplo "1.2").
 * @param title              título visible.
 * @param content            contenido completo del documento.
 * @param requiresAcceptance indica si los usuarios deben aceptar esta
 *                           versión para seguir utilizando la plataforma.
 */
public record PublishLegalDocumentRequest(
        @NotNull(message = "El tipo del documento es obligatorio.")
        LegalDocumentType type,

        @NotBlank(message = "La versión del documento es obligatoria.")
        @Size(max = 30)
        String version,

        @NotBlank(message = "El título del documento es obligatorio.")
        @Size(max = 200)
        String title,

        @NotBlank(message = "El contenido del documento es obligatorio.")
        String content,

        boolean requiresAcceptance
) {
}
