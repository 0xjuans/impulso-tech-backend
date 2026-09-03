package tech.impulso.files.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import tech.impulso.files.config.StorageProperties;

import java.io.InputStream;
import java.time.Duration;

/**
 * Implementación de {@link StorageService} contra Cloudflare R2
 * (RF-059).
 *
 * <p>Se activa cuando la propiedad {@code storage.r2.bucket} está
 * definida. Utiliza el SDK oficial de AWS S3 aprovechando que R2 expone
 * una API compatible.</p>
 */
@Service
@ConditionalOnProperty(prefix = "storage.r2", name = "bucket")
public class R2StorageService implements StorageService {

    private final S3Client client;
    private final S3Presigner presigner;
    private final StorageProperties properties;

    public R2StorageService(S3Client client, S3Presigner presigner, StorageProperties properties) {
        this.client = client;
        this.presigner = presigner;
        this.properties = properties;
    }

    @Override
    public void upload(String objectKey, String contentType, long sizeBytes, InputStream content) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .contentType(contentType)
                .contentLength(sizeBytes)
                .build();
        client.putObject(request, RequestBody.fromInputStream(content, sizeBytes));
    }

    @Override
    public String presignedDownloadUrl(String objectKey, Duration ttl) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build();
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(getRequest)
                .build();
        return presigner.presignGetObject(presignRequest).url().toString();
    }

    @Override
    public void delete(String objectKey) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(properties.bucket())
                .key(objectKey)
                .build();
        client.deleteObject(request);
    }
}
