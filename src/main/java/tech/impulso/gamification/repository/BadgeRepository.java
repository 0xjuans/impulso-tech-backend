package tech.impulso.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.gamification.entity.Badge;
import tech.impulso.gamification.entity.BadgeTrigger;

import java.util.List;

/**
 * Repositorio de persistencia para {@link Badge}.
 */
@Repository
public interface BadgeRepository extends JpaRepository<Badge, Long> {

    /**
     * Devuelve todas las insignias activas ordenadas por rareza y
     * nombre. Se utiliza en el catálogo público.
     *
     * @return insignias activas.
     */
    List<Badge> findByActiveTrueOrderByRarityAscNameAsc();

    /**
     * Devuelve las insignias activas asociadas a un disparador concreto.
     * Se utiliza para evaluar qué insignias corresponden tras un evento.
     *
     * @param triggerType tipo de disparador a evaluar.
     * @return insignias activas con ese disparador.
     */
    List<Badge> findByTriggerTypeAndActiveTrue(BadgeTrigger triggerType);
}
