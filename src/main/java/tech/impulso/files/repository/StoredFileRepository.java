package tech.impulso.files.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.files.entity.FilePurpose;
import tech.impulso.files.entity.StoredFile;

/**
 * Repositorio de persistencia para {@link StoredFile}.
 */
@Repository
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    /**
     * Devuelve los archivos del propietario indicado, más recientes
     * primero según el {@link Pageable}.
     */
    Page<StoredFile> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Devuelve los archivos del propietario indicado filtrados por
     * propósito.
     */
    Page<StoredFile> findByOwnerIdAndPurpose(Long ownerId, FilePurpose purpose, Pageable pageable);
}
