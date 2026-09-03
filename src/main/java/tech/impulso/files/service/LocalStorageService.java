package tech.impulso.files.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.impulso.common.exception.BusinessException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;

/**
 * Implementación local de {@link StorageService} para entornos de
 * desarrollo sin credenciales de Cloudflare R2 (RF-059).
 *
 * <p>Guarda los archivos bajo el directorio temporal del sistema y los
 * expone mediante una URL {@code file://} que sólo tiene sentido en la
 * máquina del desarrollador. No debe utilizarse en producción; sirve
 * únicamente para probar el ciclo completo de subida y borrado.</p>
 */
@Service
@ConditionalOnMissingBean(value = StorageService.class,
        ignored = LocalStorageService.class)
public class LocalStorageService implements StorageService {

    /** Directorio raíz donde se persisten los archivos localmente. */
    private final Path root;

    public LocalStorageService() {
        this.root = Paths.get(System.getProperty("java.io.tmpdir"), "impulso-tech-files");
        try {
            Files.createDirectories(this.root);
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible inicializar el almacenamiento local: " + e.getMessage());
        }
    }

    @Override
    public void upload(String objectKey, String contentType, long sizeBytes, InputStream content) {
        Path target = resolve(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible guardar el archivo localmente: " + e.getMessage());
        }
    }

    @Override
    public String presignedDownloadUrl(String objectKey, Duration ttl) {
        Path target = resolve(objectKey);
        return target.toUri().toString();
    }

    @Override
    public void delete(String objectKey) {
        try {
            Files.deleteIfExists(resolve(objectKey));
        } catch (IOException e) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible eliminar el archivo local: " + e.getMessage());
        }
    }

    private Path resolve(String objectKey) {
        Path candidate = root.resolve(objectKey).normalize();
        if (!candidate.startsWith(root)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "La ruta del archivo no es válida.");
        }
        return candidate;
    }
}
