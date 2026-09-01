-- =============================================================================
-- V11 — Privacidad del ranking (RF-021 / RF-049).
--
-- Añade a la tabla de usuarios la preferencia de visibilidad pública en los
-- rankings. Por defecto todos los usuarios aparecen; cada estudiante puede
-- optar por ocultarse desde su perfil sin dejar de acumular XP y progreso.
-- =============================================================================

ALTER TABLE users
    ADD COLUMN show_in_ranking BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN users.show_in_ranking IS 'Indica si el usuario acepta aparecer en los rankings públicos.';
