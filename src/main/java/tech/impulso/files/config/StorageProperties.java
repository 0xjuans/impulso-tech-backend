package tech.impulso.files.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración externa del almacenamiento de archivos (RF-059).
 *
 * <p>Se leen desde variables de entorno con prefijo {@code storage.r2.*}.
 * Cuando ninguna variable está definida, el sistema recae en el
 * almacenamiento local para desarrollo.</p>
 *
 * @param endpoint  URL base del servicio S3-compatible (Cloudflare R2).
 * @param region    región reportada al firmar peticiones; para R2 se
 *                  utiliza {@code auto}.
 * @param bucket    nombre del bucket.
 * @param accessKey identificador de la credencial de acceso.
 * @param secretKey secreto asociado a la credencial.
 * @param publicBaseUrl URL pública base opcional, para servir archivos
 *                  con acceso público sin firma.
 */
@ConfigurationProperties(prefix = "storage.r2")
public record StorageProperties(
        String endpoint,
        String region,
        String bucket,
        String accessKey,
        String secretKey,
        String publicBaseUrl
) {

    /**
     * Indica si la configuración está completa para hablar con R2.
     */
    public boolean isConfigured() {
        return endpoint != null && !endpoint.isBlank()
                && bucket != null && !bucket.isBlank()
                && accessKey != null && !accessKey.isBlank()
                && secretKey != null && !secretKey.isBlank();
    }
}
