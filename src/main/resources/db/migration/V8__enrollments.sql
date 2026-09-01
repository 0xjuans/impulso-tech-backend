-- =============================================================================
-- V8 — Inscripciones y seguimiento de progreso (RF-011).
--
-- Registra la inscripción de un estudiante a un curso y las lecciones
-- completadas. El progreso agregado (porcentaje, lecciones completadas,
-- estado del curso) se calcula a partir de estas tablas.
-- =============================================================================

CREATE TABLE enrollments (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    course_id         BIGINT       NOT NULL,
    status            VARCHAR(20)  NOT NULL DEFAULT 'INSCRITO',
    started_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at      TIMESTAMPTZ,
    last_accessed_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_enrollments_user    FOREIGN KEY (user_id)   REFERENCES users (id)   ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course  FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT uk_enrollments_user_course UNIQUE (user_id, course_id),
    CONSTRAINT ck_enrollments_status  CHECK (status IN ('INSCRITO', 'EN_PROGRESO', 'COMPLETADO'))
);

CREATE INDEX ix_enrollments_user   ON enrollments (user_id);
CREATE INDEX ix_enrollments_course ON enrollments (course_id);

COMMENT ON TABLE  enrollments                  IS 'Inscripciones de estudiantes en cursos.';
COMMENT ON COLUMN enrollments.status           IS 'Estado del avance del estudiante en el curso.';
COMMENT ON COLUMN enrollments.last_accessed_at IS 'Fecha del último acceso o avance del estudiante en el curso.';

CREATE TABLE lesson_completions (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    lesson_id     BIGINT      NOT NULL,
    completed_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lesson_completions_user   FOREIGN KEY (user_id)   REFERENCES users (id)   ON DELETE CASCADE,
    CONSTRAINT fk_lesson_completions_lesson FOREIGN KEY (lesson_id) REFERENCES lessons (id) ON DELETE CASCADE,
    CONSTRAINT uk_lesson_completions_user_lesson UNIQUE (user_id, lesson_id)
);

CREATE INDEX ix_lesson_completions_user   ON lesson_completions (user_id);
CREATE INDEX ix_lesson_completions_lesson ON lesson_completions (lesson_id);

COMMENT ON TABLE lesson_completions IS 'Marcas de lecciones completadas por los estudiantes.';
