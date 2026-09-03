-- =============================================================================
-- V25 — Mascota IA: conversaciones y mensajes por usuario (RF-017, RF-051).
--
-- Cada usuario mantiene un historial de conversaciones con la mascota. Las
-- conversaciones pueden anclarse a un contexto de aprendizaje (una lección,
-- un curso, un reto, un proyecto) para que la IA disponga de la información
-- de referencia. Los mensajes conservan el rol del emisor y el contenido
-- literal intercambiado.
-- =============================================================================

CREATE TABLE ai_conversations (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title         VARCHAR(200) NOT NULL,
    context_type  VARCHAR(30),
    context_id    BIGINT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    closed_at     TIMESTAMPTZ
);

CREATE INDEX ix_ai_conversations_user
    ON ai_conversations (user_id, updated_at DESC);

CREATE TABLE ai_messages (
    id              BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT      NOT NULL REFERENCES ai_conversations(id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL,
    content         TEXT        NOT NULL,
    tokens_used     INTEGER,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_ai_messages_conversation
    ON ai_messages (conversation_id, created_at ASC);
