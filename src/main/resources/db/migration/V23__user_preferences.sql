-- =============================================================================
-- V23 — Preferencias personales del usuario (RF-060).
--
-- Cada usuario tiene una única fila con sus preferencias. Se utiliza
-- MapsId para compartir el identificador con la tabla de usuarios.
-- =============================================================================

CREATE TABLE user_preferences (
    user_id                        BIGINT       PRIMARY KEY,
    notify_progress                BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_challenges              BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_evaluations             BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_achievements            BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_reminders               BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_mascot                  BOOLEAN      NOT NULL DEFAULT TRUE,
    notify_by_email                BOOLEAN      NOT NULL DEFAULT FALSE,
    ai_mascot_enabled              BOOLEAN      NOT NULL DEFAULT TRUE,
    profile_public                 BOOLEAN      NOT NULL DEFAULT TRUE,
    preferred_language             VARCHAR(10)  NOT NULL DEFAULT 'es',
    updated_at                     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_preferences_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

COMMENT ON TABLE  user_preferences                    IS 'Preferencias personales de comunicación y privacidad de cada usuario.';
COMMENT ON COLUMN user_preferences.notify_progress    IS 'Habilita las notificaciones relacionadas con el progreso del estudiante.';
COMMENT ON COLUMN user_preferences.notify_challenges  IS 'Habilita las notificaciones relacionadas con retos.';
COMMENT ON COLUMN user_preferences.notify_evaluations IS 'Habilita las notificaciones relacionadas con evaluaciones.';
COMMENT ON COLUMN user_preferences.notify_achievements IS 'Habilita las notificaciones de logros, insignias y recompensas.';
COMMENT ON COLUMN user_preferences.notify_reminders   IS 'Habilita los recordatorios generados por la plataforma.';
COMMENT ON COLUMN user_preferences.notify_mascot      IS 'Habilita los mensajes proactivos de la mascota virtual.';
COMMENT ON COLUMN user_preferences.notify_by_email    IS 'Indica si el usuario desea recibir notificaciones también por correo.';
COMMENT ON COLUMN user_preferences.ai_mascot_enabled  IS 'Indica si el usuario desea utilizar la mascota virtual con inteligencia artificial.';
COMMENT ON COLUMN user_preferences.profile_public     IS 'Indica si el perfil del usuario se muestra públicamente.';
COMMENT ON COLUMN user_preferences.preferred_language IS 'Código ISO del idioma preferido para la interfaz y las comunicaciones.';
