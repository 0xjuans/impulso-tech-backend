-- =============================================================================
-- V9 — Sistema de experiencia y niveles (RF-018 / RF-047).
--
-- Se almacenan dos tablas complementarias:
--   * user_xp: agregado por usuario con el total de XP acumulada y el nivel
--              actual. Facilita las consultas rápidas al perfil y al ranking.
--   * xp_events: registro histórico de cada otorgamiento de XP, con la fuente
--                (lección, curso, actividad, etc.) para auditoría y detección
--                de duplicados.
-- =============================================================================

CREATE TABLE user_xp (
    user_id       BIGINT       PRIMARY KEY,
    total_xp      INTEGER      NOT NULL DEFAULT 0,
    current_level INTEGER      NOT NULL DEFAULT 1,
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_xp_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_user_xp_total CHECK (total_xp >= 0),
    CONSTRAINT ck_user_xp_level CHECK (current_level >= 1)
);

COMMENT ON TABLE  user_xp               IS 'XP acumulada y nivel actual de cada usuario.';
COMMENT ON COLUMN user_xp.total_xp      IS 'Total de experiencia acumulada por el usuario.';
COMMENT ON COLUMN user_xp.current_level IS 'Nivel calculado a partir de la XP total.';

CREATE TABLE xp_events (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    source_type  VARCHAR(40)  NOT NULL,
    source_id    BIGINT,
    xp_awarded   INTEGER      NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_xp_events_user  FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_xp_events_award CHECK (xp_awarded > 0),
    -- Evita otorgar XP más de una vez por el mismo evento (idempotencia):
    -- una lección o un curso sólo pueden generar experiencia una vez por usuario.
    CONSTRAINT uk_xp_events_source UNIQUE (user_id, source_type, source_id)
);

CREATE INDEX ix_xp_events_user       ON xp_events (user_id);
CREATE INDEX ix_xp_events_created_at ON xp_events (created_at DESC);

COMMENT ON TABLE  xp_events             IS 'Registro histórico de los otorgamientos de XP por evento.';
COMMENT ON COLUMN xp_events.source_type IS 'Origen del otorgamiento (LESSON_COMPLETED, COURSE_COMPLETED, etc.).';
COMMENT ON COLUMN xp_events.source_id   IS 'Identificador del recurso que originó el otorgamiento.';
