package tech.impulso.gamification.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.gamification.entity.XpEvent;
import tech.impulso.gamification.entity.XpSource;

/**
 * Repositorio de persistencia para {@link XpEvent}.
 */
@Repository
public interface XpEventRepository extends JpaRepository<XpEvent, Long> {

    /**
     * Indica si ya existe un evento que haya otorgado XP al usuario por
     * una combinación específica de fuente e identificador. Se utiliza
     * para garantizar la idempotencia antes de intentar la inserción.
     *
     * @param userId     identificador del usuario.
     * @param sourceType fuente que originaría el otorgamiento.
     * @param sourceId   identificador del recurso asociado.
     * @return {@code true} cuando ya existe un evento equivalente.
     */
    boolean existsByUserIdAndSourceTypeAndSourceId(Long userId, XpSource sourceType, Long sourceId);

    /**
     * Devuelve los eventos de XP de un usuario ordenados según la
     * configuración de paginación.
     *
     * @param userId   identificador del usuario.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con los eventos del usuario.
     */
    Page<XpEvent> findByUserId(Long userId, Pageable pageable);
}
