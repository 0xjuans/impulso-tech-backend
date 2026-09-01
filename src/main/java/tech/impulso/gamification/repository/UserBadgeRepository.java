package tech.impulso.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.gamification.entity.UserBadge;

import java.util.List;
import java.util.Set;

/**
 * Repositorio de persistencia para {@link UserBadge}.
 */
@Repository
public interface UserBadgeRepository extends JpaRepository<UserBadge, Long> {

    /**
     * Devuelve todas las insignias obtenidas por un usuario, ordenadas
     * de la más reciente a la más antigua.
     *
     * @param userId identificador del usuario.
     * @return insignias obtenidas por el usuario.
     */
    List<UserBadge> findByUserIdOrderByAwardedAtDesc(Long userId);

    /**
     * Devuelve los identificadores de las insignias que un usuario ya
     * posee. Se utiliza durante la evaluación para omitir las insignias
     * ya otorgadas sin cargar los objetos completos.
     *
     * @param userId identificador del usuario.
     * @return identificadores de las insignias obtenidas.
     */
    @Query("select ub.badge.id from UserBadge ub where ub.user.id = :userId")
    Set<Long> findBadgeIdsByUserId(@Param("userId") Long userId);
}
