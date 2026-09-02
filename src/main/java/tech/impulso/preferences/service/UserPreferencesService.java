package tech.impulso.preferences.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.preferences.dto.UpdatePreferencesRequest;
import tech.impulso.preferences.dto.UserPreferencesResponse;
import tech.impulso.preferences.entity.UserPreferences;
import tech.impulso.preferences.repository.UserPreferencesRepository;
import tech.impulso.users.entity.User;

/**
 * Servicio encargado de la gestión de preferencias del usuario
 * autenticado (RF-060).
 */
@Service
public class UserPreferencesService {

    private final UserPreferencesRepository repository;
    private final CurrentUserService currentUserService;

    public UserPreferencesService(UserPreferencesRepository repository,
                                  CurrentUserService currentUserService) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve las preferencias del usuario autenticado. Crea la fila
     * con valores por defecto si aún no existe.
     */
    @Transactional
    public UserPreferencesResponse getMyPreferences() {
        return UserPreferencesResponse.from(findOrCreate(currentUserService.requireAuthenticatedUser()));
    }

    /**
     * Actualiza las preferencias del usuario autenticado. Los valores
     * {@code null} en el request se dejan sin modificar.
     */
    @Transactional
    public UserPreferencesResponse updateMyPreferences(UpdatePreferencesRequest request) {
        UserPreferences preferences = findOrCreate(currentUserService.requireAuthenticatedUser());

        if (request.notifyProgress() != null) {
            preferences.setNotifyProgress(request.notifyProgress());
        }
        if (request.notifyChallenges() != null) {
            preferences.setNotifyChallenges(request.notifyChallenges());
        }
        if (request.notifyEvaluations() != null) {
            preferences.setNotifyEvaluations(request.notifyEvaluations());
        }
        if (request.notifyAchievements() != null) {
            preferences.setNotifyAchievements(request.notifyAchievements());
        }
        if (request.notifyReminders() != null) {
            preferences.setNotifyReminders(request.notifyReminders());
        }
        if (request.notifyMascot() != null) {
            preferences.setNotifyMascot(request.notifyMascot());
        }
        if (request.notifyByEmail() != null) {
            preferences.setNotifyByEmail(request.notifyByEmail());
        }
        if (request.aiMascotEnabled() != null) {
            preferences.setAiMascotEnabled(request.aiMascotEnabled());
        }
        if (request.profilePublic() != null) {
            preferences.setProfilePublic(request.profilePublic());
        }
        if (request.preferredLanguage() != null) {
            preferences.setPreferredLanguage(request.preferredLanguage().trim());
        }

        return UserPreferencesResponse.from(preferences);
    }

    private UserPreferences findOrCreate(User user) {
        return repository.findById(user.getId()).orElseGet(() -> {
            UserPreferences prefs = new UserPreferences();
            prefs.setUser(user);
            return repository.save(prefs);
        });
    }
}
