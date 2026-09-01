package tech.impulso.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.community.entity.CommunityReport;
import tech.impulso.community.entity.ReportStatus;
import tech.impulso.community.entity.ReportTargetType;

/**
 * Repositorio de persistencia para {@link CommunityReport}.
 */
@Repository
public interface CommunityReportRepository extends JpaRepository<CommunityReport, Long> {

    /**
     * Indica si un usuario ya reportó un contenido específico. Sirve
     * para evitar duplicados antes de intentar la inserción.
     *
     * @param reporterId identificador del usuario reportante.
     * @param targetType tipo del contenido reportado.
     * @param targetId   identificador del contenido reportado.
     * @return {@code true} cuando ya existe un reporte equivalente.
     */
    boolean existsByReporterIdAndTargetTypeAndTargetId(Long reporterId,
                                                       ReportTargetType targetType,
                                                       Long targetId);

    /**
     * Búsqueda paginada de reportes aplicando filtros opcionales.
     *
     * @param status     estado a filtrar, opcional.
     * @param targetType tipo del contenido reportado, opcional.
     * @param pageable   configuración de paginación y ordenamiento.
     * @return página con los reportes coincidentes.
     */
    @Query("""
            select r from CommunityReport r
            where (:status     is null or r.status     = :status)
              and (:targetType is null or r.targetType = :targetType)
            """)
    Page<CommunityReport> search(@Param("status") ReportStatus status,
                                 @Param("targetType") ReportTargetType targetType,
                                 Pageable pageable);
}
