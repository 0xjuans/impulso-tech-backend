-- =============================================================================
-- V3 — Registro de actividad administrativa (RF-056).
--
-- Almacena las acciones sensibles ejecutadas por administradores sobre los
-- recursos de la plataforma. Cada registro es inmutable y conserva quién
-- realizó la acción, sobre qué recurso, en qué momento y detalles adicionales.
-- =============================================================================

CREATE TABLE admin_activity_log (
    id           BIGSERIAL PRIMARY KEY,
    -- Puede ser NULL cuando la acción la ejecuta un proceso automático del
    -- sistema (por ejemplo, la creación del primer administrador).
    admin_id     BIGINT,
    action       VARCHAR(80)  NOT NULL,
    target_type  VARCHAR(60),
    target_id    BIGINT,
    details      TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_admin_activity_admin FOREIGN KEY (admin_id) REFERENCES users (id) ON DELETE SET NULL
);

CREATE INDEX ix_admin_activity_admin      ON admin_activity_log (admin_id);
CREATE INDEX ix_admin_activity_action     ON admin_activity_log (action);
CREATE INDEX ix_admin_activity_target     ON admin_activity_log (target_type, target_id);
CREATE INDEX ix_admin_activity_created_at ON admin_activity_log (created_at DESC);

COMMENT ON TABLE  admin_activity_log             IS 'Registro de acciones administrativas relevantes ejecutadas en Impulso Tech.';
COMMENT ON COLUMN admin_activity_log.admin_id    IS 'Usuario administrador responsable de la acción. Nulo cuando la acción es ejecutada por un proceso del sistema.';
COMMENT ON COLUMN admin_activity_log.action      IS 'Código corto de la acción realizada.';
COMMENT ON COLUMN admin_activity_log.target_type IS 'Tipo del recurso afectado (USER, COURSE, etc.).';
COMMENT ON COLUMN admin_activity_log.target_id   IS 'Identificador interno del recurso afectado.';
COMMENT ON COLUMN admin_activity_log.details     IS 'Detalles adicionales en formato texto libre o JSON.';
