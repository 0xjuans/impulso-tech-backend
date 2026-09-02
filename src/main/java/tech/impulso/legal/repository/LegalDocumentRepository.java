package tech.impulso.legal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.legal.entity.LegalDocument;
import tech.impulso.legal.entity.LegalDocumentType;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de persistencia para {@link LegalDocument}.
 */
@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {

    /**
     * Devuelve la versión vigente (no retirada) del tipo de documento
     * indicado.
     */
    @Query("""
            select d from LegalDocument d
            where d.type = :type
              and d.retiredAt is null
            """)
    Optional<LegalDocument> findCurrentByType(@Param("type") LegalDocumentType type);

    /**
     * Devuelve todas las versiones vigentes de todos los tipos de
     * documento legal.
     */
    @Query("select d from LegalDocument d where d.retiredAt is null")
    List<LegalDocument> findAllCurrent();

    /**
     * Devuelve el historial de versiones publicadas para un tipo de
     * documento, ordenado por fecha de publicación descendente.
     */
    List<LegalDocument> findByTypeOrderByPublishedAtDesc(LegalDocumentType type);
}
