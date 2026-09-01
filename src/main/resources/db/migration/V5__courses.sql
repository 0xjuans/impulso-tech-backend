-- =============================================================================
-- V5 — Módulo de cursos (RF-040).
--
-- Un curso agrupa contenidos educativos estructurados en torno a un objetivo
-- de aprendizaje específico. Puede formar parte de una ruta de aprendizaje o
-- existir de manera independiente. La estructura interna del curso (módulos
-- y lecciones) se gestiona mediante entidades posteriores.
-- =============================================================================

CREATE TABLE courses (
    id                       BIGSERIAL PRIMARY KEY,
    name                     VARCHAR(150) NOT NULL,
    description              TEXT         NOT NULL,
    objective                TEXT,
    cover_image_url          VARCHAR(500),
    difficulty               VARCHAR(20)  NOT NULL,
    estimated_duration_hours INTEGER,
    technology               VARCHAR(120),
    learning_route_id        BIGINT,
    instructor_id            BIGINT       NOT NULL,
    status                   VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    generates_certificate    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_courses_learning_route FOREIGN KEY (learning_route_id) REFERENCES learning_routes (id) ON DELETE SET NULL,
    CONSTRAINT fk_courses_instructor     FOREIGN KEY (instructor_id) REFERENCES users (id),
    CONSTRAINT ck_courses_difficulty     CHECK (difficulty IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT ck_courses_status         CHECK (status IN ('BORRADOR', 'PUBLICADO', 'DESHABILITADO'))
);

CREATE INDEX ix_courses_status         ON courses (status);
CREATE INDEX ix_courses_instructor     ON courses (instructor_id);
CREATE INDEX ix_courses_learning_route ON courses (learning_route_id);

COMMENT ON TABLE  courses                          IS 'Cursos disponibles en Impulso Tech.';
COMMENT ON COLUMN courses.learning_route_id        IS 'Ruta de aprendizaje a la que pertenece el curso. Nulo cuando el curso es independiente.';
COMMENT ON COLUMN courses.generates_certificate    IS 'Indica si al completar el curso se genera un certificado digital verificable.';
COMMENT ON COLUMN courses.estimated_duration_hours IS 'Duración estimada del curso expresada en horas.';
