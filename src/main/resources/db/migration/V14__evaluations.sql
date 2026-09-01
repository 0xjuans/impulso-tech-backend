-- =============================================================================
-- V14 — Evaluaciones (RF-015 / RF-043).
--
-- Una evaluación agrupa varias preguntas auto-corregibles asociadas a una
-- lección. El estudiante inicia un intento, responde todas las preguntas y
-- el sistema calcula el resultado (puntaje total, porcentaje y aprobación).
--
-- Las respuestas del intento se almacenan como JSON dentro del propio
-- registro para simplificar el modelo inicial. Los tipos de pregunta
-- soportados reutilizan la misma estructura de configuración que las
-- actividades (RF-042).
-- =============================================================================

CREATE TABLE evaluations (
    id                  BIGSERIAL PRIMARY KEY,
    lesson_id           BIGINT       NOT NULL,
    name                VARCHAR(180) NOT NULL,
    description         TEXT,
    instructions        TEXT,
    time_limit_minutes  INTEGER,
    passing_percentage  INTEGER      NOT NULL DEFAULT 60,
    max_attempts        INTEGER      NOT NULL DEFAULT 1,
    order_index         INTEGER      NOT NULL,
    status              VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_evaluations_lesson    FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT ck_evaluations_status    CHECK (status IN ('BORRADOR', 'PUBLICADO', 'DESHABILITADO')),
    CONSTRAINT ck_evaluations_passing   CHECK (passing_percentage BETWEEN 0 AND 100),
    CONSTRAINT ck_evaluations_attempts  CHECK (max_attempts > 0),
    CONSTRAINT ck_evaluations_time      CHECK (time_limit_minutes IS NULL OR time_limit_minutes > 0),
    CONSTRAINT uk_evaluations_order     UNIQUE (lesson_id, order_index)
);

CREATE INDEX ix_evaluations_lesson ON evaluations (lesson_id);

COMMENT ON TABLE  evaluations                    IS 'Evaluaciones que agrupan varias preguntas dentro de una lección.';
COMMENT ON COLUMN evaluations.time_limit_minutes IS 'Tiempo máximo permitido en minutos; NULL indica sin límite.';
COMMENT ON COLUMN evaluations.passing_percentage IS 'Porcentaje mínimo requerido para aprobar la evaluación.';
COMMENT ON COLUMN evaluations.max_attempts       IS 'Número máximo de intentos que el estudiante puede realizar.';

CREATE TABLE evaluation_questions (
    id             BIGSERIAL PRIMARY KEY,
    evaluation_id  BIGINT       NOT NULL,
    order_index    INTEGER      NOT NULL,
    type           VARCHAR(30)  NOT NULL,
    question_text  TEXT         NOT NULL,
    score          INTEGER      NOT NULL DEFAULT 1,
    config         TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_evaluation_questions_evaluation FOREIGN KEY (evaluation_id) REFERENCES evaluations (id) ON DELETE CASCADE,
    CONSTRAINT ck_evaluation_questions_type       CHECK (type IN ('SELECCION_MULTIPLE', 'VERDADERO_FALSO', 'RESPUESTA_CORTA')),
    CONSTRAINT ck_evaluation_questions_score      CHECK (score > 0),
    CONSTRAINT uk_evaluation_questions_order      UNIQUE (evaluation_id, order_index)
);

CREATE INDEX ix_evaluation_questions_evaluation ON evaluation_questions (evaluation_id);

COMMENT ON TABLE  evaluation_questions        IS 'Preguntas que componen una evaluación.';
COMMENT ON COLUMN evaluation_questions.config IS 'Configuración específica del tipo (respuesta correcta y demás), en JSON.';

CREATE TABLE evaluation_attempts (
    id                   BIGSERIAL PRIMARY KEY,
    evaluation_id        BIGINT       NOT NULL,
    user_id              BIGINT       NOT NULL,
    started_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    finished_at          TIMESTAMPTZ,
    total_score          INTEGER      NOT NULL DEFAULT 0,
    max_possible_score   INTEGER      NOT NULL DEFAULT 0,
    percentage           INTEGER      NOT NULL DEFAULT 0,
    passed               BOOLEAN      NOT NULL DEFAULT FALSE,
    answers              TEXT,
    CONSTRAINT fk_evaluation_attempts_evaluation FOREIGN KEY (evaluation_id) REFERENCES evaluations (id) ON DELETE CASCADE,
    CONSTRAINT fk_evaluation_attempts_user       FOREIGN KEY (user_id)       REFERENCES users       (id) ON DELETE CASCADE,
    CONSTRAINT ck_evaluation_attempts_percentage CHECK (percentage BETWEEN 0 AND 100)
);

CREATE INDEX ix_evaluation_attempts_user_eval ON evaluation_attempts (user_id, evaluation_id);

COMMENT ON TABLE  evaluation_attempts         IS 'Intentos realizados por los estudiantes sobre una evaluación.';
COMMENT ON COLUMN evaluation_attempts.answers IS 'Respuestas enviadas por el estudiante, en JSON, indexadas por identificador de pregunta.';
