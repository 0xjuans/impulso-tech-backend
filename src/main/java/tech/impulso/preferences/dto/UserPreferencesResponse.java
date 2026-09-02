package tech.impulso.preferences.dto;

import tech.impulso.preferences.entity.UserPreferences;

import java.time.OffsetDateTime;

/**
 * Representación pública de las preferencias del usuario.
 */
public record UserPreferencesResponse(
        boolean notifyProgress,
        boolean notifyChallenges,
        boolean notifyEvaluations,
        boolean notifyAchievements,
        boolean notifyReminders,
        boolean notifyMascot,
        boolean notifyByEmail,
        boolean aiMascotEnabled,
        boolean profilePublic,
        String preferredLanguage,
        OffsetDateTime updatedAt
) {

    public static UserPreferencesResponse from(UserPreferences preferences) {
        return new UserPreferencesResponse(
                preferences.isNotifyProgress(),
                preferences.isNotifyChallenges(),
                preferences.isNotifyEvaluations(),
                preferences.isNotifyAchievements(),
                preferences.isNotifyReminders(),
                preferences.isNotifyMascot(),
                preferences.isNotifyByEmail(),
                preferences.isAiMascotEnabled(),
                preferences.isProfilePublic(),
                preferences.getPreferredLanguage(),
                preferences.getUpdatedAt()
        );
    }
}
