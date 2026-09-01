package tech.impulso.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.gamification.entity.UserXp;

/**
 * Repositorio de persistencia para {@link UserXp}.
 */
@Repository
public interface UserXpRepository extends JpaRepository<UserXp, Long> {
}
