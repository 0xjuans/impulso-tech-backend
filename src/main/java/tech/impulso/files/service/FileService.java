package tech.impulso.files.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.files.dto.StoredFileResponse;
import tech.impulso.files.entity.FilePurpose;
import tech.impulso.files.entity.StoredFile;
import tech.impulso.files.repository.StoredFileRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Servicio de gestión de archivos almacenados (RF-059).
 *
 * <p>Coordina la validación por propósito (tamaño y tipos MIME
 * permitidos, §33 del CLAUDE.md), la asignación de llaves únicas, la
 * subida al {@link StorageService}, la persistencia de la metadata y
 * la generación de URLs firmadas temporales para acceder al binario.</p>
 */
@Service
public class FileService {

    /** Duración de las URLs firmadas para descarga. */
    private static final Duration DOWNLOAD_URL_TTL = Duration.ofMinutes(10);

    /** Tipos MIME permitidos por propósito. */
    private static final Map<FilePurpose, Set<String>> ALLOWED_TYPES = Map.of(
            FilePurpose.PROFILE_PHOTO, Set.of("image/jpeg", "image/png", "image/webp"),
            FilePurpose.COURSE_COVER, Set.of("image/jpeg", "image/png", "image/webp"),
            FilePurpose.RESOURCE, Set.of("application/pdf", "image/jpeg", "image/png",
                    "image/webp", "video/mp4", "text/plain"),
            FilePurpose.SUBMISSION, Set.of("application/pdf", "application/zip",
                    "text/plain", "image/jpeg", "image/png"),
            FilePurpose.SUPPORT_ATTACHMENT, Set.of("image/jpeg", "image/png",
                    "application/pdf", "text/plain"),
            FilePurpose.GENERAL, Set.of("image/jpeg", "image/png", "application/pdf",
                    "text/plain")
    );

    /** Tamaño máximo permitido por propósito, en bytes. */
    private static final Map<FilePurpose, Long> MAX_SIZE_BYTES = Map.of(
            FilePurpose.PROFILE_PHOTO, 2L * 1024 * 1024,
            FilePurpose.COURSE_COVER, 5L * 1024 * 1024,
            FilePurpose.RESOURCE, 50L * 1024 * 1024,
            FilePurpose.SUBMISSION, 20L * 1024 * 1024,
            FilePurpose.SUPPORT_ATTACHMENT, 5L * 1024 * 1024,
            FilePurpose.GENERAL, 10L * 1024 * 1024
    );

    /** Propósitos cuyos archivos son legibles por cualquier usuario autenticado. */
    private static final Set<FilePurpose> PUBLIC_PURPOSES = Set.of(
            FilePurpose.PROFILE_PHOTO,
            FilePurpose.COURSE_COVER,
            FilePurpose.RESOURCE
    );

    private final StoredFileRepository repository;
    private final StorageService storageService;
    private final CurrentUserService currentUserService;

    public FileService(StoredFileRepository repository,
                       StorageService storageService,
                       CurrentUserService currentUserService) {
        this.repository = repository;
        this.storageService = storageService;
        this.currentUserService = currentUserService;
    }

    /**
     * Sube un archivo aplicando validaciones por propósito.
     */
    @Transactional
    public StoredFileResponse upload(MultipartFile file, FilePurpose purpose) {
        User owner = currentUserService.requireAuthenticatedUser();
        FilePurpose resolvedPurpose = purpose == null ? FilePurpose.GENERAL : purpose;

        if (file == null || file.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Debe adjuntar un archivo no vacío.");
        }

        String contentType = file.getContentType();
        Set<String> allowed = ALLOWED_TYPES.getOrDefault(resolvedPurpose, Set.of());
        if (contentType == null || !allowed.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException(HttpStatus.UNSUPPORTED_MEDIA_TYPE,
                    "Tipo de archivo no permitido para el propósito indicado.");
        }

        long maxSize = MAX_SIZE_BYTES.getOrDefault(resolvedPurpose, 5L * 1024 * 1024);
        if (file.getSize() > maxSize) {
            throw new BusinessException(HttpStatus.PAYLOAD_TOO_LARGE,
                    "El archivo supera el tamaño máximo permitido (%d bytes).".formatted(maxSize));
        }

        String key = buildObjectKey(resolvedPurpose, owner.getId(), file.getOriginalFilename());
        try {
            storageService.upload(key, contentType, file.getSize(), file.getInputStream());
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible leer el contenido del archivo: " + e.getMessage());
        }

        StoredFile stored = new StoredFile();
        stored.setObjectKey(key);
        stored.setOriginalName(sanitizeName(file.getOriginalFilename()));
        stored.setContentType(contentType);
        stored.setSizeBytes(file.getSize());
        stored.setPurpose(resolvedPurpose);
        stored.setOwner(owner);
        stored.setPublicAccess(PUBLIC_PURPOSES.contains(resolvedPurpose));
        stored = repository.save(stored);

        String url = storageService.presignedDownloadUrl(stored.getObjectKey(), DOWNLOAD_URL_TTL);
        return StoredFileResponse.withUrl(stored, url,
                OffsetDateTime.now(ZoneOffset.UTC).plus(DOWNLOAD_URL_TTL));
    }

    /**
     * Devuelve la metadata y una URL firmada temporal para descargar
     * el archivo indicado.
     */
    @Transactional(readOnly = true)
    public StoredFileResponse download(Long id) {
        User caller = currentUserService.requireAuthenticatedUser();
        StoredFile file = requireVisibleFile(id, caller);
        String url = storageService.presignedDownloadUrl(file.getObjectKey(), DOWNLOAD_URL_TTL);
        return StoredFileResponse.withUrl(file, url,
                OffsetDateTime.now(ZoneOffset.UTC).plus(DOWNLOAD_URL_TTL));
    }

    /**
     * Elimina el archivo del almacenamiento y su metadata. Requiere
     * propiedad o rol de administrador.
     */
    @Transactional
    public void delete(Long id) {
        User caller = currentUserService.requireAuthenticatedUser();
        StoredFile file = repository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El archivo indicado no existe."));
        boolean isOwner = file.getOwner().getId().equals(caller.getId());
        boolean isAdmin = caller.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "No tiene permiso para eliminar este archivo.");
        }
        storageService.delete(file.getObjectKey());
        repository.delete(file);
    }

    /**
     * Devuelve los archivos subidos por el usuario autenticado.
     */
    @Transactional(readOnly = true)
    public Page<StoredFileResponse> listMine(FilePurpose purpose, Pageable pageable) {
        User caller = currentUserService.requireAuthenticatedUser();
        Page<StoredFile> page = purpose == null
                ? repository.findByOwnerId(caller.getId(), pageable)
                : repository.findByOwnerIdAndPurpose(caller.getId(), purpose, pageable);
        return page.map(StoredFileResponse::metadata);
    }

    /**
     * Verifica el permiso de lectura sobre un archivo. Los archivos con
     * {@code publicAccess = true} son legibles por cualquier usuario
     * autenticado; el resto sólo por el propietario o el administrador.
     */
    private StoredFile requireVisibleFile(Long id, User caller) {
        StoredFile file = repository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El archivo indicado no existe."));
        boolean isOwner = file.getOwner().getId().equals(caller.getId());
        boolean isAdmin = caller.getRole() == Role.ADMINISTRADOR;
        if (!file.isPublicAccess() && !isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "No tiene permiso para acceder a este archivo.");
        }
        return file;
    }

    /**
     * Genera una llave única con estructura {@code purpose/ownerId/uuid.ext}
     * para no exponer el nombre original y evitar colisiones.
     */
    private static String buildObjectKey(FilePurpose purpose, Long ownerId, String originalName) {
        String extension = extensionFrom(originalName);
        return "%s/%d/%s%s".formatted(
                purpose.name().toLowerCase(Locale.ROOT),
                ownerId,
                UUID.randomUUID(),
                extension
        );
    }

    private static String extensionFrom(String originalName) {
        if (originalName == null) {
            return "";
        }
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) {
            return "";
        }
        String ext = originalName.substring(dot).toLowerCase(Locale.ROOT);
        return ext.matches("\\.[a-z0-9]{1,10}") ? ext : "";
    }

    /**
     * Depura el nombre original para conservar sólo caracteres seguros.
     */
    private static String sanitizeName(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "archivo";
        }
        String base = originalName.replaceAll("[\\\\/]+", "-");
        return base.length() > 255 ? base.substring(base.length() - 255) : base;
    }
}
