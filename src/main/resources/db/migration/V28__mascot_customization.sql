-- =============================================================================
-- V28 — Personalización de la mascota y recompensas por progreso
-- (RF-022, RF-050, RF-052).
--
-- Los estudiantes desbloquean ítems visuales para la mascota (piel, sombrero,
-- accesorio, fondo) a medida que progresan: por nivel alcanzado, por XP
-- acumulado o por obtener una insignia concreta. La plataforma nunca utiliza
-- una moneda interna; los ítems se conceden automáticamente al cumplir el
-- requisito (RF-031 del CLAUDE.md).
-- =============================================================================

CREATE TABLE mascot_items (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(60)  NOT NULL UNIQUE,
    name                VARCHAR(120) NOT NULL,
    description         TEXT,
    slot                VARCHAR(30)  NOT NULL,
    image_url           VARCHAR(500),
    unlock_type         VARCHAR(30)  NOT NULL,
    unlock_level        INTEGER,
    unlock_xp           INTEGER,
    unlock_badge_id     BIGINT REFERENCES badges(id) ON DELETE SET NULL,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_mascot_items_slot   ON mascot_items (slot);
CREATE INDEX ix_mascot_items_active ON mascot_items (active);

CREATE TABLE mascot_unlocks (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    item_id       BIGINT      NOT NULL REFERENCES mascot_items(id) ON DELETE CASCADE,
    unlocked_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, item_id)
);

CREATE INDEX ix_mascot_unlocks_user ON mascot_unlocks (user_id);

CREATE TABLE mascot_customization (
    user_id             BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    skin_item_id        BIGINT REFERENCES mascot_items(id) ON DELETE SET NULL,
    hat_item_id         BIGINT REFERENCES mascot_items(id) ON DELETE SET NULL,
    accessory_item_id   BIGINT REFERENCES mascot_items(id) ON DELETE SET NULL,
    background_item_id  BIGINT REFERENCES mascot_items(id) ON DELETE SET NULL,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
