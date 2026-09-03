package tech.impulso.mascotcustomization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.mascotcustomization.entity.MascotUnlock;

import java.util.List;

/**
 * Repositorio de persistencia para {@link MascotUnlock}.
 */
@Repository
public interface MascotUnlockRepository extends JpaRepository<MascotUnlock, Long> {

    /**
     * Indica si el usuario ya tiene desbloqueado el ítem indicado.
     */
    boolean existsByUserIdAndItemId(Long userId, Long itemId);

    /**
     * Devuelve los identificadores de ítems desbloqueados por el
     * usuario. Se utiliza para marcar el estado de cada ítem en el
     * catálogo sin cargar la relación completa.
     */
    @Query("select u.item.id from MascotUnlock u where u.user.id = :userId")
    List<Long> findUnlockedItemIds(@Param("userId") Long userId);
}
