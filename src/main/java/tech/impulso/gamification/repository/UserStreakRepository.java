package tech.impulso.gamification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.gamification.entity.UserStreak;

/**
 * Repositorio de persistencia para {@link UserStreak}.
 */
@Repository
public interface UserStreakRepository extends JpaRepository<UserStreak, Long> {
}
