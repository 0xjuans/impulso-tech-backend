package tech.impulso.files.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Configuración de los beans de acceso a Cloudflare R2 (RF-059).
 *
 * <p>Sólo crea los clientes cuando la configuración está completa. En
 * ausencia de credenciales, la aplicación arranca correctamente y se
 * usa el almacenamiento local en su lugar.</p>
 */
@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

    /**
     * Cliente síncrono de S3 apuntando al endpoint de R2.
     */
    @Bean(destroyMethod = "close")
    public S3Client s3Client(StorageProperties properties) {
        if (!properties.isConfigured()) {
            return null;
        }
        return S3Client.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(defaultRegion(properties.region())))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    /**
     * Generador de URLs firmadas para descarga temporal.
     */
    @Bean(destroyMethod = "close")
    public S3Presigner s3Presigner(StorageProperties properties) {
        if (!properties.isConfigured()) {
            return null;
        }
        return S3Presigner.builder()
                .endpointOverride(URI.create(properties.endpoint()))
                .region(Region.of(defaultRegion(properties.region())))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
    }

    /** Devuelve la región suministrada o {@code auto} por defecto. */
    private static String defaultRegion(String region) {
        return region == null || region.isBlank() ? "auto" : region;
    }
}
