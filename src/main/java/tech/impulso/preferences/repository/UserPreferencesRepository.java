package tech.impulso.preferences.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.preferences.entity.UserPreferences;

/**
 * Repositorio de persistencia para {@link UserPreferences}.
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
}
