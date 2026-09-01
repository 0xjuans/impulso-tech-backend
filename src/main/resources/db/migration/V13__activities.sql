-- =============================================================================
-- V13 — Actividades y ejercicios (RF-042).
--
-- Las actividades permiten evaluar la comprensión del estudiante dentro de
-- una lección. En esta versión inicial se soportan tres tipos con
-- auto-corrección: selección múltiple, verdadero/falso y respuesta corta.
--
-- La configuración específica de cada tipo (opciones, respuestas correctas,
-- etc.) se almacena como JSON en la columna `config` para permitir extender
-- los tipos soportados sin modificar el esquema.
-- =============================================================================

CREATE TABLE activities (
    id            BIGSERIAL PRIMARY KEY,
    lesson_id     BIGINT       NOT NULL,
    name          VARCHAR(180) NOT NULL,
    description   TEXT,
    instructions  TEXT,
    type          VARCHAR(30)  NOT NULL,
    difficulty    VARCHAR(20)  NOT NULL,
    max_score     INTEGER      NOT NULL DEFAULT 100,
    xp_reward     INTEGER      NOT NULL DEFAULT 5,
    max_attempts  INTEGER,
    order_index   INTEGER      NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    config        TEXT         NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_activities_lesson     FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT ck_activities_type       CHECK (type IN ('SELECCION_MULTIPLE', 'VERDADERO_FALSO', 'RESPUESTA_CORTA')),
    CONSTRAINT ck_activities_difficulty CHECK (difficulty IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT ck_activities_status     CHECK (status IN ('BORRADOR', 'PUBLICADO', 'DESHABILITADO')),
    CONSTRAINT ck_activities_score      CHECK (max_score > 0),
    CONSTRAINT ck_activities_xp         CHECK (xp_reward >= 0),
    CONSTRAINT ck_activities_attempts   CHECK (max_attempts IS NULL OR max_attempts > 0),
    CONSTRAINT uk_activities_order      UNIQUE (lesson_id, order_index)
);

CREATE INDEX ix_activities_lesson ON activities (lesson_id);

COMMENT ON TABLE  activities              IS 'Actividades y ejercicios asociados a las lecciones.';
COMMENT ON COLUMN activities.type         IS 'Tipo de actividad; determina cómo se interpreta la configuración.';
COMMENT ON COLUMN activities.config       IS 'Configuración específica del tipo, almacenada como JSON.';
COMMENT ON COLUMN activities.max_attempts IS 'Máximo de intentos permitidos; NULL indica intentos ilimitados.';
COMMENT ON COLUMN activities.xp_reward    IS 'XP otorgada al estudiante la primera vez que responde correctamente.';

CREATE TABLE activity_attempts (
    id            BIGSERIAL PRIMARY KEY,
    activity_id   BIGINT       NOT NULL,
    user_id       BIGINT       NOT NULL,
    answer        TEXT         NOT NULL,
    correct       BOOLEAN      NOT NULL,
    score         INTEGER      NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_activity_attempts_activity FOREIGN KEY (activity_id) REFERENCES activities (id) ON DELETE CASCADE,
    CONSTRAINT fk_activity_attempts_user     FOREIGN KEY (user_id)     REFERENCES users      (id) ON DELETE CASCADE,
    CONSTRAINT ck_activity_attempts_score    CHECK (score >= 0)
);

CREATE INDEX ix_activity_attempts_user_activity ON activity_attempts (user_id, activity_id);

COMMENT ON TABLE  activity_attempts        IS 'Historial de intentos realizados por los estudiantes en cada actividad.';
COMMENT ON COLUMN activity_attempts.answer IS 'Respuesta enviada por el estudiante en formato JSON.';
