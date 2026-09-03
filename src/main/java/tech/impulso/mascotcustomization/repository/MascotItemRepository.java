package tech.impulso.mascotcustomization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.mascotcustomization.entity.MascotItem;

import java.util.List;

/**
 * Repositorio de persistencia para {@link MascotItem}.
 */
@Repository
public interface MascotItemRepository extends JpaRepository<MascotItem, Long> {

    /**
     * Devuelve los ítems activos del catálogo. Se utiliza en la vista
     * del estudiante.
     */
    List<MascotItem> findByActiveTrueOrderBySlotAscNameAsc();

    /**
     * Indica si existe un ítem con el código indicado.
     */
    boolean existsByCode(String code);
}
