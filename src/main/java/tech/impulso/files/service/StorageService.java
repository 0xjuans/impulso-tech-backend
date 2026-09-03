package tech.impulso.files.service;

import java.io.InputStream;
import java.time.Duration;

/**
 * Puerta de entrada al proveedor de almacenamiento de archivos
 * (RF-059, §24 del CLAUDE.md).
 *
 * <p>El backend mantiene el control total del acceso: las credenciales
 * nunca llegan al frontend y todo acceso al binario se hace mediante
 * URLs firmadas temporales generadas por esta interfaz. La
 * implementación por defecto se conecta con Cloudflare R2; puede
 * sustituirse por un almacenamiento local durante desarrollo cuando no
 * hay credenciales configuradas.</p>
 */
public interface StorageService {

    /**
     * Sube un archivo al almacenamiento y devuelve la llave asignada.
     *
     * @param objectKey   llave con la que se guardará el objeto.
     * @param contentType tipo MIME reportado por el cliente.
     * @param sizeBytes   tamaño en bytes.
     * @param content     flujo binario del contenido.
     */
    void upload(String objectKey, String contentType, long sizeBytes, InputStream content);

    /**
     * Genera una URL firmada temporal para descargar el archivo.
     *
     * @param objectKey  llave del objeto.
     * @param ttl        duración de la firma antes de expirar.
     * @return URL absoluta que puede consumir el navegador del cliente.
     */
    String presignedDownloadUrl(String objectKey, Duration ttl);

    /**
     * Elimina el objeto del almacenamiento. No falla si el objeto ya no
     * existe.
     */
    void delete(String objectKey);
}
