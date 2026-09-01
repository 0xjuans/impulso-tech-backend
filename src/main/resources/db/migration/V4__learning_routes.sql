-- =============================================================================
-- V4 — Módulo de rutas de aprendizaje (RF-039).
--
-- Una ruta de aprendizaje agrupa contenidos organizados de manera
-- secuencial para guiar al estudiante en la adquisición progresiva de
-- conocimientos y habilidades. Cada ruta pertenece a un instructor
-- responsable y expone un estado propio dentro del ciclo de vida del
-- contenido (borrador, publicado o deshabilitado).
-- =============================================================================

CREATE TABLE learning_routes (
    id                       BIGSERIAL PRIMARY KEY,
    name                     VARCHAR(150) NOT NULL,
    description              TEXT         NOT NULL,
    objective                TEXT,
    cover_image_url          VARCHAR(500),
    difficulty               VARCHAR(20)  NOT NULL,
    estimated_duration_hours INTEGER,
    technologies             VARCHAR(300),
    status                   VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    instructor_id            BIGINT       NOT NULL,
    created_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_learning_routes_instructor FOREIGN KEY (instructor_id) REFERENCES users (id),
    CONSTRAINT ck_learning_routes_difficulty CHECK (difficulty IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT ck_learning_routes_status     CHECK (status IN ('BORRADOR', 'PUBLICADO', 'DESHABILITADO'))
);

CREATE INDEX ix_learning_routes_status     ON learning_routes (status);
CREATE INDEX ix_learning_routes_instructor ON learning_routes (instructor_id);

COMMENT ON TABLE  learning_routes                          IS 'Rutas de aprendizaje disponibles en Impulso Tech.';
COMMENT ON COLUMN learning_routes.technologies             IS 'Lista de tecnologías o lenguajes relacionados con la ruta (texto separado por comas).';
COMMENT ON COLUMN learning_routes.estimated_duration_hours IS 'Duración estimada de la ruta expresada en horas.';
