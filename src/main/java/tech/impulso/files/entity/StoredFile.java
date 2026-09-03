package tech.impulso.files.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Metadatos de un archivo almacenado en Cloudflare R2 (RF-059).
 *
 * <p>Esta entidad conserva la información de negocio del archivo:
 * quién lo subió, para qué se usa, tipo MIME, tamaño y la llave con la
 * que se localiza dentro del bucket. El contenido binario nunca se
 * guarda en la base de datos; se accede a él exclusivamente a través
 * del {@code StorageService}.</p>
 */
@Entity
@Table(name = "stored_files")
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Llave del objeto dentro del bucket. Se genera aleatoriamente al
     * subir el archivo para evitar colisiones y no exponer nombres
     * predecibles.
     */
    @Column(name = "object_key", nullable = false, unique = true, length = 300)
    private String objectKey;

    /** Nombre original del archivo, conservado para descarga y auditoría. */
    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    /** Tipo MIME reportado, ya validado contra la lista permitida por propósito. */
    @Column(name = "content_type", nullable = false, length = 120)
    private String contentType;

    /** Tamaño en bytes del archivo, validado contra el límite del propósito. */
    @Column(name = "size_bytes", nullable = false)
    private long sizeBytes;

    /** Propósito funcional que determinó las validaciones aplicadas. */
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false, length = 40)
    private FilePurpose purpose;

    /** Usuario que subió y es propietario del archivo. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * Indica si el archivo es accesible mediante URL firmada por
     * cualquier usuario autenticado. Cuando es {@code false} sólo el
     * propietario y el administrador pueden obtener la URL.
     */
    @Column(name = "public_access", nullable = false)
    private boolean publicAccess;

    /** Fecha de creación del registro. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    void onCreate() {
        this.createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() { return id; }
    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String originalName) { this.originalName = originalName; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }
    public FilePurpose getPurpose() { return purpose; }
    public void setPurpose(FilePurpose purpose) { this.purpose = purpose; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public boolean isPublicAccess() { return publicAccess; }
    public void setPublicAccess(boolean publicAccess) { this.publicAccess = publicAccess; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
