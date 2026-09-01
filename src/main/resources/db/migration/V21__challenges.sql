-- =============================================================================
-- V21 — Retos de programación (RF-014).
--
-- Cada reto es un desafío independiente creado por un instructor. El
-- estudiante envía intentos con su solución en el lenguaje permitido; el
-- instructor revisa y otorga aprobación o rechazo con retroalimentación.
-- La ejecución automática de pruebas se incorporará junto con RF-037.
-- =============================================================================

CREATE TABLE challenges (
    id                    BIGSERIAL PRIMARY KEY,
    name                  VARCHAR(180) NOT NULL,
    description           TEXT         NOT NULL,
    objective             TEXT,
    instructions          TEXT,
    difficulty            VARCHAR(20)  NOT NULL,
    allowed_languages     VARCHAR(300) NOT NULL,
    io_examples           TEXT,
    restrictions          TEXT,
    public_test_cases     TEXT,
    hidden_test_cases     TEXT,
    xp_reward             INTEGER      NOT NULL DEFAULT 50,
    estimated_minutes     INTEGER,
    status                VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    learning_route_id     BIGINT,
    course_id             BIGINT,
    module_id             BIGINT,
    lesson_id             BIGINT,
    instructor_id         BIGINT       NOT NULL,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_challenges_instructor     FOREIGN KEY (instructor_id)     REFERENCES users            (id),
    CONSTRAINT fk_challenges_learning_route FOREIGN KEY (learning_route_id) REFERENCES learning_routes  (id) ON DELETE SET NULL,
    CONSTRAINT fk_challenges_course         FOREIGN KEY (course_id)         REFERENCES courses          (id) ON DELETE SET NULL,
    CONSTRAINT fk_challenges_module         FOREIGN KEY (module_id)         REFERENCES course_modules   (id) ON DELETE SET NULL,
    CONSTRAINT fk_challenges_lesson         FOREIGN KEY (lesson_id)         REFERENCES lessons          (id) ON DELETE SET NULL,
    CONSTRAINT ck_challenges_difficulty     CHECK (difficulty IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT ck_challenges_status         CHECK (status IN ('BORRADOR', 'PUBLICADO', 'DESHABILITADO')),
    CONSTRAINT ck_challenges_xp             CHECK (xp_reward >= 0),
    CONSTRAINT ck_challenges_minutes        CHECK (estimated_minutes IS NULL OR estimated_minutes > 0)
);

CREATE INDEX ix_challenges_status         ON challenges (status);
CREATE INDEX ix_challenges_difficulty     ON challenges (difficulty);
CREATE INDEX ix_challenges_instructor     ON challenges (instructor_id);
CREATE INDEX ix_challenges_course         ON challenges (course_id);

COMMENT ON TABLE  challenges                   IS 'Retos de programación publicados por instructores.';
COMMENT ON COLUMN challenges.allowed_languages IS 'Lenguajes de programación permitidos, en formato CSV.';
COMMENT ON COLUMN challenges.io_examples       IS 'Ejemplos de entrada y salida esperados.';
COMMENT ON COLUMN challenges.public_test_cases IS 'Casos de prueba visibles al estudiante como referencia.';
COMMENT ON COLUMN challenges.hidden_test_cases IS 'Casos de prueba ocultos utilizados para calificación.';
COMMENT ON COLUMN challenges.xp_reward         IS 'XP otorgada al estudiante la primera vez que resuelve el reto correctamente.';

CREATE TABLE challenge_attempts (
    id             BIGSERIAL PRIMARY KEY,
    challenge_id   BIGINT       NOT NULL,
    user_id        BIGINT       NOT NULL,
    attempt_number INTEGER      NOT NULL,
    language       VARCHAR(60)  NOT NULL,
    code           TEXT         NOT NULL,
    status         VARCHAR(30)  NOT NULL DEFAULT 'PENDIENTE',
    feedback       TEXT,
    reviewed_by    BIGINT,
    reviewed_at    TIMESTAMPTZ,
    submitted_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_challenge_attempts_challenge FOREIGN KEY (challenge_id) REFERENCES challenges (id) ON DELETE CASCADE,
    CONSTRAINT fk_challenge_attempts_user      FOREIGN KEY (user_id)      REFERENCES users      (id) ON DELETE CASCADE,
    CONSTRAINT fk_challenge_attempts_reviewer  FOREIGN KEY (reviewed_by)  REFERENCES users      (id) ON DELETE SET NULL,
    CONSTRAINT ck_challenge_attempts_status    CHECK (status IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    CONSTRAINT uk_challenge_attempts_user_number UNIQUE (challenge_id, user_id, attempt_number)
);

CREATE INDEX ix_challenge_attempts_challenge  ON challenge_attempts (challenge_id);
CREATE INDEX ix_challenge_attempts_user       ON challenge_attempts (user_id, challenge_id);
CREATE INDEX ix_challenge_attempts_status     ON challenge_attempts (status);

COMMENT ON TABLE  challenge_attempts        IS 'Intentos realizados por los estudiantes sobre los retos publicados.';
COMMENT ON COLUMN challenge_attempts.status IS 'Estado del intento: pendiente de revisión, aprobado o rechazado.';
