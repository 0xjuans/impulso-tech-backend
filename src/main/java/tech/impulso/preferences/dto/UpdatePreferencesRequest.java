package tech.impulso.preferences.dto;

import jakarta.validation.constraints.Pattern;

/**
 * Datos que se pueden modificar de las preferencias del usuario. Los
 * valores {@code null} indican que ese campo no debe actualizarse.
 */
public record UpdatePreferencesRequest(
        Boolean notifyProgress,
        Boolean notifyChallenges,
        Boolean notifyEvaluations,
        Boolean notifyAchievements,
        Boolean notifyReminders,
        Boolean notifyMascot,
        Boolean notifyByEmail,
        Boolean aiMascotEnabled,
        Boolean profilePublic,

        @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$",
                message = "El idioma preferido debe ser un código ISO válido (por ejemplo, 'es' o 'es-CO').")
        String preferredLanguage
) {
}
