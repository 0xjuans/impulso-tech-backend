-- =============================================================================
-- V15 — Sistema de notificaciones (RF-026).
--
-- Registra las notificaciones que se generan automáticamente cuando ocurren
-- eventos relevantes para el usuario: subida de nivel, insignias obtenidas,
-- cursos completados, evaluaciones aprobadas o reprobadas, hitos de racha,
-- entre otros. El destinatario puede consultarlas, marcarlas como leídas o
-- eliminarlas desde el centro de notificaciones.
-- =============================================================================

CREATE TABLE notifications (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    type          VARCHAR(40)  NOT NULL,
    title         VARCHAR(200) NOT NULL,
    message       TEXT         NOT NULL,
    related_type  VARCHAR(40),
    related_id    BIGINT,
    read_at       TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE INDEX ix_notifications_user_created ON notifications (user_id, created_at DESC);
CREATE INDEX ix_notifications_user_unread  ON notifications (user_id) WHERE read_at IS NULL;

COMMENT ON TABLE  notifications              IS 'Notificaciones dirigidas a un usuario específico.';
COMMENT ON COLUMN notifications.type         IS 'Tipo funcional de la notificación (BADGE_AWARDED, LEVEL_UP, etc.).';
COMMENT ON COLUMN notifications.related_type IS 'Tipo del recurso relacionado (COURSE, BADGE, EVALUATION, etc.), cuando aplique.';
COMMENT ON COLUMN notifications.related_id   IS 'Identificador del recurso relacionado, cuando aplique.';
COMMENT ON COLUMN notifications.read_at      IS 'Momento en que el usuario marcó la notificación como leída.';
