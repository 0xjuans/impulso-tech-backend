-- =============================================================================
-- V17 — Recursos favoritos (RF-025).
--
-- Cada estudiante puede marcar recursos educativos como favoritos para
-- acceder rápidamente a ellos desde su sección personal. La restricción
-- única impide favoritos duplicados por par (usuario, recurso).
-- =============================================================================

CREATE TABLE resource_favorites (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    resource_id BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_resource_favorites_user     FOREIGN KEY (user_id)     REFERENCES users                 (id) ON DELETE CASCADE,
    CONSTRAINT fk_resource_favorites_resource FOREIGN KEY (resource_id) REFERENCES educational_resources (id) ON DELETE CASCADE,
    CONSTRAINT uk_resource_favorites_user_resource UNIQUE (user_id, resource_id)
);

CREATE INDEX ix_resource_favorites_user ON resource_favorites (user_id, created_at DESC);

COMMENT ON TABLE resource_favorites IS 'Recursos que cada estudiante ha marcado como favoritos.';
