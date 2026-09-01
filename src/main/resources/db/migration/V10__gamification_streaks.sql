-- =============================================================================
-- V10 — Rachas de aprendizaje (RF-020 / RF-048).
--
-- Mantiene por usuario la racha actual (días consecutivos con actividad
-- válida), el récord histórico y la fecha del último día que contó como
-- actividad. La racha se recalcula automáticamente cuando el estudiante
-- realiza una actividad válida de aprendizaje (por ejemplo, completar una
-- lección).
-- =============================================================================

CREATE TABLE user_streaks (
    user_id            BIGINT       PRIMARY KEY,
    current_streak     INTEGER      NOT NULL DEFAULT 0,
    longest_streak     INTEGER      NOT NULL DEFAULT 0,
    last_activity_date DATE,
    streak_started_on  DATE,
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_streaks_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_user_streaks_current CHECK (current_streak >= 0),
    CONSTRAINT ck_user_streaks_longest CHECK (longest_streak >= 0)
);

COMMENT ON TABLE  user_streaks                    IS 'Rachas de aprendizaje por usuario.';
COMMENT ON COLUMN user_streaks.current_streak     IS 'Cantidad actual de días consecutivos con actividad válida.';
COMMENT ON COLUMN user_streaks.longest_streak     IS 'Récord histórico de días consecutivos alcanzado por el usuario.';
COMMENT ON COLUMN user_streaks.last_activity_date IS 'Fecha (UTC) del último día que contó como actividad válida.';
COMMENT ON COLUMN user_streaks.streak_started_on  IS 'Fecha (UTC) en que comenzó la racha actual.';
