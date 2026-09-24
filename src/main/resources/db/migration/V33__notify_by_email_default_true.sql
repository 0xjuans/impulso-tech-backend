-- =============================================================================
-- V33 — Preferencia notify_by_email pasa a estar activa por defecto.
--
-- El toggle de "Correo electrónico" en el perfil se creó recientemente y
-- se detectó que la mayoría de usuarios esperan recibir los avisos y
-- mensajes directos por correo sin tener que activarlo manualmente.
--
-- La migración cambia el default de la columna a TRUE y actualiza a
-- todas las filas existentes. Como el toggle fue introducido en la misma
-- sesión, ningún usuario lo desactivó de forma consciente todavía; el
-- riesgo de sobrescribir una decisión explícita es nulo. Si en un
-- futuro se desea evitar sobrescrituras, deberá agregarse una columna
-- {@code notify_by_email_touched_at} para distinguir el estado inicial.
-- =============================================================================

ALTER TABLE user_preferences
    ALTER COLUMN notify_by_email SET DEFAULT TRUE;

UPDATE user_preferences
SET notify_by_email = TRUE
WHERE notify_by_email = FALSE;
