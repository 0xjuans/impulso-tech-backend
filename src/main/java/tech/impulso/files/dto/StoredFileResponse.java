package tech.impulso.files.dto;

import tech.impulso.files.entity.FilePurpose;
import tech.impulso.files.entity.StoredFile;

import java.time.OffsetDateTime;

/**
 * Representación pública de un archivo almacenado (RF-059).
 *
 * @param id           identificador interno.
 * @param originalName nombre con el que el usuario subió el archivo.
 * @param contentType  tipo MIME.
 * @param sizeBytes    tamaño en bytes.
 * @param purpose      propósito funcional.
 * @param ownerId      identificador del propietario.
 * @param publicAccess indica si cualquier usuario autenticado puede
 *                     obtener la URL firmada.
 * @param createdAt    fecha de subida.
 * @param downloadUrl  URL firmada temporal para descargar el archivo.
 *                     Es {@code null} cuando el cliente no solicitó la
 *                     firma o cuando no tiene permisos para obtenerla.
 * @param expiresAt    momento en que expira la URL firmada.
 */
public record StoredFileResponse(
        Long id,
        String originalName,
        String contentType,
        long sizeBytes,
        FilePurpose purpose,
        Long ownerId,
        boolean publicAccess,
        OffsetDateTime createdAt,
        String downloadUrl,
        OffsetDateTime expiresAt
) {

    public static StoredFileResponse metadata(StoredFile file) {
        return new StoredFileResponse(
                file.getId(),
                file.getOriginalName(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getPurpose(),
                file.getOwner().getId(),
                file.isPublicAccess(),
                file.getCreatedAt(),
                null,
                null
        );
    }

    public static StoredFileResponse withUrl(StoredFile file, String url, OffsetDateTime expiresAt) {
        return new StoredFileResponse(
                file.getId(),
                file.getOriginalName(),
                file.getContentType(),
                file.getSizeBytes(),
                file.getPurpose(),
                file.getOwner().getId(),
                file.isPublicAccess(),
                file.getCreatedAt(),
                url,
                expiresAt
        );
    }
}
