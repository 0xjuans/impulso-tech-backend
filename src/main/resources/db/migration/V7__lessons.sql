-- =============================================================================
-- V7 — Lecciones (RF-041).
--
-- Una lección es la unidad principal de contenido teórico y práctico dentro
-- de un módulo. En esta primera versión el contenido educativo se almacena
-- como texto (Markdown o HTML). Los recursos multimedia complejos se
-- gestionarán posteriormente mediante entidades independientes.
-- =============================================================================

CREATE TABLE lessons (
    id                          BIGSERIAL PRIMARY KEY,
    module_id                   BIGINT       NOT NULL,
    title                       VARCHAR(180) NOT NULL,
    description                 TEXT,
    objective                   TEXT,
    content                     TEXT,
    estimated_duration_minutes  INTEGER,
    order_index                 INTEGER      NOT NULL,
    is_optional                 BOOLEAN      NOT NULL DEFAULT FALSE,
    status                      VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lessons_module FOREIGN KEY (module_id) REFERENCES course_modules (id) ON DELETE CASCADE,
    CONSTRAINT ck_lessons_status CHECK (status IN ('BORRADOR', 'PUBLICADO', 'DESHABILITADO')),
    CONSTRAINT uk_lessons_order  UNIQUE (module_id, order_index)
);

CREATE INDEX ix_lessons_module ON lessons (module_id);

COMMENT ON TABLE  lessons                            IS 'Lecciones que componen los módulos de un curso.';
COMMENT ON COLUMN lessons.content                    IS 'Contenido educativo de la lección en Markdown o HTML.';
COMMENT ON COLUMN lessons.estimated_duration_minutes IS 'Duración estimada de la lección expresada en minutos.';
COMMENT ON COLUMN lessons.order_index                IS 'Posición de la lección dentro del módulo (base 1).';
COMMENT ON COLUMN lessons.is_optional                IS 'Indica si la lección es opcional para completar el módulo.';
