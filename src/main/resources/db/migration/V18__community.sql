-- =============================================================================
-- V18 — Comunidad y foro (RF-034), núcleo funcional.
--
-- Se crean tres tablas: publicaciones, respuestas y votos "útil". Los
-- reportes de contenido inapropiado se agregarán en una migración posterior
-- para mantener este cambio acotado a la interacción principal.
-- =============================================================================

CREATE TABLE community_posts (
    id                 BIGSERIAL PRIMARY KEY,
    author_id          BIGINT       NOT NULL,
    title              VARCHAR(200) NOT NULL,
    description        TEXT         NOT NULL,
    code_snippet       TEXT,
    -- Etiquetas almacenadas como texto separado por comas para
    -- simplificar el modelo inicial. Migrará a text[] si el volumen lo
    -- justifica.
    tags               VARCHAR(500),
    related_type       VARCHAR(30),
    related_id         BIGINT,
    accepted_reply_id  BIGINT,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_community_posts_author       FOREIGN KEY (author_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT ck_community_posts_related_type CHECK (related_type IS NULL OR related_type IN (
        'LEARNING_ROUTE', 'COURSE', 'MODULE', 'LESSON',
        'ACTIVITY', 'CHALLENGE', 'LAB', 'EVALUATION', 'PROJECT'
    ))
);

CREATE INDEX ix_community_posts_author      ON community_posts (author_id);
CREATE INDEX ix_community_posts_created_at  ON community_posts (created_at DESC);
CREATE INDEX ix_community_posts_related     ON community_posts (related_type, related_id);

COMMENT ON TABLE  community_posts              IS 'Publicaciones de la comunidad y foro de aprendizaje.';
COMMENT ON COLUMN community_posts.tags         IS 'Etiquetas asociadas a la publicación, en formato CSV.';
COMMENT ON COLUMN community_posts.related_type IS 'Tipo del contenido educativo relacionado con la publicación, si aplica.';
COMMENT ON COLUMN community_posts.related_id   IS 'Identificador del contenido educativo relacionado, si aplica.';

CREATE TABLE community_replies (
    id             BIGSERIAL PRIMARY KEY,
    post_id        BIGINT       NOT NULL,
    author_id      BIGINT       NOT NULL,
    content        TEXT         NOT NULL,
    code_snippet   TEXT,
    helpful_count  INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_community_replies_post    FOREIGN KEY (post_id)   REFERENCES community_posts (id) ON DELETE CASCADE,
    CONSTRAINT fk_community_replies_author  FOREIGN KEY (author_id) REFERENCES users            (id) ON DELETE CASCADE,
    CONSTRAINT ck_community_replies_helpful CHECK (helpful_count >= 0)
);

CREATE INDEX ix_community_replies_post ON community_replies (post_id, created_at ASC);

COMMENT ON TABLE  community_replies              IS 'Respuestas dentro de una publicación de la comunidad.';
COMMENT ON COLUMN community_replies.helpful_count IS 'Cantidad de usuarios que han marcado la respuesta como útil.';

-- FK diferida para permitir la referencia circular post → reply aceptada.
ALTER TABLE community_posts
    ADD CONSTRAINT fk_community_posts_accepted_reply
    FOREIGN KEY (accepted_reply_id) REFERENCES community_replies (id) ON DELETE SET NULL;

CREATE TABLE community_helpful_votes (
    id          BIGSERIAL PRIMARY KEY,
    reply_id    BIGINT       NOT NULL,
    user_id     BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_community_helpful_reply FOREIGN KEY (reply_id) REFERENCES community_replies (id) ON DELETE CASCADE,
    CONSTRAINT fk_community_helpful_user  FOREIGN KEY (user_id)  REFERENCES users             (id) ON DELETE CASCADE,
    CONSTRAINT uk_community_helpful_reply_user UNIQUE (reply_id, user_id)
);

COMMENT ON TABLE community_helpful_votes IS 'Marcas de "útil" registradas por usuarios sobre respuestas.';
