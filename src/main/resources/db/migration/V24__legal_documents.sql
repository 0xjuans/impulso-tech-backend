-- =============================================================================
-- V24 — Documentos legales y consentimientos (RF-062).
--
-- Se publican versiones de términos y condiciones, política de privacidad y
-- otros documentos relevantes. Cada documento activo registra la aceptación
-- de cada usuario, dejando evidencia de la versión aceptada y de la fecha.
-- =============================================================================

CREATE TABLE legal_documents (
    id            BIGSERIAL PRIMARY KEY,
    type          VARCHAR(30)  NOT NULL,
    version       VARCHAR(30)  NOT NULL,
    title         VARCHAR(200) NOT NULL,
    content       TEXT         NOT NULL,
    requires_acceptance BOOLEAN NOT NULL DEFAULT TRUE,
    published_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    retired_at    TIMESTAMPTZ,
    published_by  BIGINT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_legal_documents_publisher FOREIGN KEY (published_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_legal_documents_type      CHECK (type IN ('TERMS', 'PRIVACY', 'CODE_OF_CONDUCT', 'DATA_PROCESSING')),
    CONSTRAINT uk_legal_documents_type_version UNIQUE (type, version)
);

CREATE INDEX ix_legal_documents_type_current ON legal_documents (type) WHERE retired_at IS NULL;

COMMENT ON TABLE  legal_documents                     IS 'Versiones publicadas de documentos legales de la plataforma.';
COMMENT ON COLUMN legal_documents.type                IS 'Tipo del documento legal (términos, privacidad, código de conducta, tratamiento de datos).';
COMMENT ON COLUMN legal_documents.requires_acceptance IS 'Indica si la publicación exige que el usuario acepte esta versión para seguir utilizando la plataforma.';
COMMENT ON COLUMN legal_documents.retired_at          IS 'Momento en que la versión dejó de ser vigente. Nulo mientras se considera actual.';

CREATE TABLE user_legal_acceptances (
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT       NOT NULL,
    document_id   BIGINT       NOT NULL,
    accepted_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    ip_address    VARCHAR(64),
    CONSTRAINT fk_user_legal_acceptances_user     FOREIGN KEY (user_id)     REFERENCES users            (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_legal_acceptances_document FOREIGN KEY (document_id) REFERENCES legal_documents  (id) ON DELETE CASCADE,
    CONSTRAINT uk_user_legal_acceptances_user_document UNIQUE (user_id, document_id)
);

CREATE INDEX ix_user_legal_acceptances_user ON user_legal_acceptances (user_id);

COMMENT ON TABLE  user_legal_acceptances            IS 'Registro de aceptaciones de documentos legales por parte de los usuarios.';
COMMENT ON COLUMN user_legal_acceptances.ip_address IS 'Dirección IP desde la cual el usuario aceptó el documento.';
