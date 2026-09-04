-- =============================================================================
-- V29 — Sistema de mensajería directa entre usuarios (RF-061).
--
-- Los mensajes se organizan en conversaciones 1 a 1 entre dos participantes.
-- Para garantizar unicidad y facilitar la búsqueda, el par de participantes
-- se guarda ordenado por id ascendente (participant_low, participant_high).
-- =============================================================================

CREATE TABLE message_conversations (
    id                 BIGSERIAL PRIMARY KEY,
    participant_low_id  BIGINT     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    participant_high_id BIGINT     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_message_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_message_conversations_order CHECK (participant_low_id < participant_high_id),
    UNIQUE (participant_low_id, participant_high_id)
);

CREATE INDEX ix_message_conversations_low
    ON message_conversations (participant_low_id, last_message_at DESC);

CREATE INDEX ix_message_conversations_high
    ON message_conversations (participant_high_id, last_message_at DESC);

CREATE TABLE messages (
    id               BIGSERIAL PRIMARY KEY,
    conversation_id  BIGINT      NOT NULL REFERENCES message_conversations(id) ON DELETE CASCADE,
    sender_id        BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content          TEXT        NOT NULL,
    sent_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    read_at          TIMESTAMPTZ
);

CREATE INDEX ix_messages_conversation
    ON messages (conversation_id, sent_at DESC);

CREATE INDEX ix_messages_sender
    ON messages (sender_id, sent_at DESC);
