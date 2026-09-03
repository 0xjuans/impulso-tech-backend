-- =============================================================================
-- V27 — Metadatos de archivos almacenados en Cloudflare R2 (RF-059).
--
-- El backend nunca expone credenciales de R2 al frontend. Esta tabla mantiene
-- la relación entre el archivo binario (identificado por su llave dentro del
-- bucket) y los datos de negocio: propietario, propósito, tipo MIME, tamaño
-- y fechas. Las URLs de acceso se generan bajo demanda con firmas temporales.
-- =============================================================================

CREATE TABLE stored_files (
    id             BIGSERIAL PRIMARY KEY,
    object_key     VARCHAR(300) NOT NULL UNIQUE,
    original_name  VARCHAR(255) NOT NULL,
    content_type   VARCHAR(120) NOT NULL,
    size_bytes     BIGINT       NOT NULL,
    purpose        VARCHAR(40)  NOT NULL,
    owner_id       BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    public_access  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX ix_stored_files_owner   ON stored_files (owner_id, created_at DESC);
CREATE INDEX ix_stored_files_purpose ON stored_files (purpose);
