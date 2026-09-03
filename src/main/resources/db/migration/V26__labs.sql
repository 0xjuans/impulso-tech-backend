-- =============================================================================
-- V26 — Laboratorios y entregas con ejecución de código (RF-013, RF-037, RF-038).
--
-- Los laboratorios son entornos prácticos donde el estudiante experimenta con
-- un lenguaje de programación. Cada laboratorio define instrucciones, código
-- inicial y opcionalmente una salida esperada para verificación automática.
-- Cada envío del estudiante persiste el código enviado y el resultado de la
-- ejecución en el sandbox aislado (RF-030), con su tiempo, exit code y logs.
-- =============================================================================

CREATE TABLE labs (
    id                    BIGSERIAL PRIMARY KEY,
    title                 VARCHAR(180) NOT NULL,
    description           TEXT         NOT NULL,
    instructions          TEXT         NOT NULL,
    language              VARCHAR(60)  NOT NULL,
    starter_code          TEXT,
    expected_output       TEXT,
    execution_timeout_ms  INTEGER      NOT NULL DEFAULT 5000,
    course_id             BIGINT REFERENCES courses(id) ON DELETE SET NULL,
    lesson_id             BIGINT REFERENCES lessons(id) ON DELETE SET NULL,
    instructor_id         BIGINT       NOT NULL REFERENCES users(id),
    status                VARCHAR(20)  NOT NULL,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_labs_instructor ON labs (instructor_id);
CREATE INDEX ix_labs_course     ON labs (course_id);
CREATE INDEX ix_labs_lesson     ON labs (lesson_id);
CREATE INDEX ix_labs_status     ON labs (status);

CREATE TABLE lab_submissions (
    id                 BIGSERIAL PRIMARY KEY,
    lab_id             BIGINT      NOT NULL REFERENCES labs(id) ON DELETE CASCADE,
    user_id            BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    submission_number  INTEGER     NOT NULL,
    code               TEXT        NOT NULL,
    stdout             TEXT,
    stderr             TEXT,
    exit_code          INTEGER,
    execution_time_ms  INTEGER,
    passed             BOOLEAN     NOT NULL DEFAULT FALSE,
    submitted_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (lab_id, user_id, submission_number)
);

CREATE INDEX ix_lab_submissions_user_lab ON lab_submissions (user_id, lab_id);
