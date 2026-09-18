package tech.impulso.admin.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.admin.entity.AdminActivityLog;

/**
 * Repositorio de persistencia para las entradas del registro de
 * actividad administrativa.
 */
@Repository
public interface AdminActivityLogRepository extends JpaRepository<AdminActivityLog, Long> {

    /**
     * Búsqueda paginada de entradas con filtros opcionales.
     *
     * @param adminId    identificador del administrador responsable, si aplica.
     * @param action     acción a filtrar, si aplica.
     * @param targetType tipo de recurso afectado, si aplica.
     * @param pageable   configuración de paginación y ordenamiento.
     * @return página con las entradas coincidentes.
     */
    @Query("""
            select a from AdminActivityLog a
            where (cast(:adminId as long) is null or a.admin.id = :adminId)
              and (cast(:action as string) is null or a.action = :action)
              and (cast(:targetType as string) is null or a.targetType = :targetType)
            """)
    Page<AdminActivityLog> search(@Param("adminId") Long adminId,
                                  @Param("action") String action,
                                  @Param("targetType") String targetType,
                                  Pageable pageable);
}
