-- =============================================================================
-- V16 — Biblioteca de recursos educativos (RF-024).
--
-- Cada recurso guarda sus metadatos y un URL de contenido. En esta primera
-- versión el URL puede apuntar a un enlace externo o a un objeto público de
-- Cloudflare R2. La carga de archivos propia se incorporará junto con el
-- módulo de archivos (RF-059).
--
-- El recurso puede vincularse opcionalmente a una ruta, curso, módulo o
-- lección. Todos los vínculos son nullables para permitir recursos
-- generales que no dependan de un contenido específico.
-- =============================================================================

CREATE TABLE educational_resources (
    id                 BIGSERIAL PRIMARY KEY,
    name               VARCHAR(180) NOT NULL,
    description        TEXT,
    type               VARCHAR(30)  NOT NULL,
    category           VARCHAR(120),
    topic              VARCHAR(150),
    technology         VARCHAR(120),
    difficulty         VARCHAR(20),
    author             VARCHAR(150),
    resource_url       VARCHAR(1000) NOT NULL,
    status             VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    published_at       TIMESTAMPTZ,
    created_by         BIGINT       NOT NULL,
    learning_route_id  BIGINT,
    course_id          BIGINT,
    module_id          BIGINT,
    lesson_id          BIGINT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_resources_creator        FOREIGN KEY (created_by)        REFERENCES users            (id),
    CONSTRAINT fk_resources_learning_route FOREIGN KEY (learning_route_id) REFERENCES learning_routes  (id) ON DELETE SET NULL,
    CONSTRAINT fk_resources_course         FOREIGN KEY (course_id)         REFERENCES courses          (id) ON DELETE SET NULL,
    CONSTRAINT fk_resources_module         FOREIGN KEY (module_id)         REFERENCES course_modules   (id) ON DELETE SET NULL,
    CONSTRAINT fk_resources_lesson         FOREIGN KEY (lesson_id)         REFERENCES lessons          (id) ON DELETE SET NULL,
    CONSTRAINT ck_resources_type           CHECK (type IN ('PDF', 'VIDEO', 'IMAGEN', 'CODIGO', 'ENLACE_EXTERNO', 'DOCUMENTO', 'GUIA')),
    CONSTRAINT ck_resources_difficulty     CHECK (difficulty IS NULL OR difficulty IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT ck_resources_status         CHECK (status IN ('BORRADOR', 'PUBLICADO', 'DESHABILITADO'))
);

CREATE INDEX ix_resources_status         ON educational_resources (status);
CREATE INDEX ix_resources_type           ON educational_resources (type);
CREATE INDEX ix_resources_course         ON educational_resources (course_id);
CREATE INDEX ix_resources_module         ON educational_resources (module_id);
CREATE INDEX ix_resources_lesson         ON educational_resources (lesson_id);
CREATE INDEX ix_resources_learning_route ON educational_resources (learning_route_id);

COMMENT ON TABLE  educational_resources           IS 'Recursos educativos disponibles en la biblioteca de Impulso Tech.';
COMMENT ON COLUMN educational_resources.type      IS 'Tipo funcional del recurso (PDF, VIDEO, IMAGEN, etc.).';
COMMENT ON COLUMN educational_resources.resource_url IS 'URL del recurso: enlace externo o URL público del objeto en R2.';
COMMENT ON COLUMN educational_resources.category  IS 'Categoría temática libre configurada por el instructor.';
COMMENT ON COLUMN educational_resources.topic     IS 'Tema o subtema al que pertenece el recurso.';
COMMENT ON COLUMN educational_resources.author    IS 'Autor del contenido; puede diferir del creador del registro.';
