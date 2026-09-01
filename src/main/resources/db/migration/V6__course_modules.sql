-- =============================================================================
-- V6 — Módulos de curso (RF-041).
--
-- Un módulo agrupa una o varias lecciones dentro de un curso y define un
-- objetivo intermedio de aprendizaje. Cada módulo mantiene un orden
-- explícito dentro del curso y puede marcarse como obligatorio u opcional.
-- =============================================================================

CREATE TABLE course_modules (
    id           BIGSERIAL PRIMARY KEY,
    course_id    BIGINT       NOT NULL,
    name         VARCHAR(150) NOT NULL,
    description  TEXT,
    objective    TEXT,
    order_index  INTEGER      NOT NULL,
    is_optional  BOOLEAN      NOT NULL DEFAULT FALSE,
    status       VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_course_modules_course FOREIGN KEY (course_id) REFERENCES courses (id) ON DELETE CASCADE,
    CONSTRAINT ck_course_modules_status CHECK (status IN ('BORRADOR', 'PUBLICADO', 'DESHABILITADO')),
    CONSTRAINT uk_course_modules_order  UNIQUE (course_id, order_index)
);

CREATE INDEX ix_course_modules_course ON course_modules (course_id);

COMMENT ON TABLE  course_modules             IS 'Módulos que componen la estructura interna de un curso.';
COMMENT ON COLUMN course_modules.order_index IS 'Posición del módulo dentro del curso (base 1).';
COMMENT ON COLUMN course_modules.is_optional IS 'Indica si el módulo es opcional para completar el curso.';
