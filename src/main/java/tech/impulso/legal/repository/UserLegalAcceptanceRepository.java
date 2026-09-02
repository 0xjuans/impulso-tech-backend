package tech.impulso.legal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.legal.entity.UserLegalAcceptance;

import java.util.List;

/**
 * Repositorio de persistencia para {@link UserLegalAcceptance}.
 */
@Repository
public interface UserLegalAcceptanceRepository extends JpaRepository<UserLegalAcceptance, Long> {

    /**
     * Devuelve las aceptaciones del usuario indicado ordenadas
     * cronológicamente descendentes.
     */
    List<UserLegalAcceptance> findByUserIdOrderByAcceptedAtDesc(Long userId);

    /**
     * Indica si el usuario ya aceptó una versión específica del
     * documento.
     */
    boolean existsByUserIdAndDocumentId(Long userId, Long documentId);

    /**
     * Devuelve los identificadores de documentos que el usuario ya
     * aceptó, útil para calcular los documentos pendientes.
     */
    @Query("select a.document.id from UserLegalAcceptance a where a.user.id = :userId")
    List<Long> findAcceptedDocumentIds(@Param("userId") Long userId);
}
