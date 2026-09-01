-- =============================================================================
-- V19 — Reportes de contenido de la comunidad (RF-034, moderación).
--
-- Los usuarios pueden reportar publicaciones o respuestas por contenido
-- inapropiado, spam u otras razones. El administrador revisa cada reporte
-- y lo marca como revisado o desestimado. La restricción única impide
-- que un mismo usuario reporte el mismo contenido más de una vez.
-- =============================================================================

CREATE TABLE community_reports (
    id              BIGSERIAL PRIMARY KEY,
    reporter_id     BIGINT       NOT NULL,
    target_type     VARCHAR(10)  NOT NULL,
    target_id       BIGINT       NOT NULL,
    reason          TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    reviewed_by     BIGINT,
    reviewed_at     TIMESTAMPTZ,
    admin_notes     TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_community_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_community_reports_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_community_reports_target   CHECK (target_type IN ('POST', 'REPLY')),
    CONSTRAINT ck_community_reports_status   CHECK (status IN ('PENDIENTE', 'REVISADO', 'DESESTIMADO')),
    CONSTRAINT uk_community_reports_unique_report UNIQUE (reporter_id, target_type, target_id)
);

CREATE INDEX ix_community_reports_status  ON community_reports (status);
CREATE INDEX ix_community_reports_target  ON community_reports (target_type, target_id);
CREATE INDEX ix_community_reports_created ON community_reports (created_at DESC);

COMMENT ON TABLE  community_reports              IS 'Reportes de contenido inapropiado en el foro de la comunidad.';
COMMENT ON COLUMN community_reports.target_type  IS 'Tipo del contenido reportado (POST o REPLY).';
COMMENT ON COLUMN community_reports.status       IS 'Estado del reporte (PENDIENTE, REVISADO, DESESTIMADO).';
COMMENT ON COLUMN community_reports.admin_notes  IS 'Observaciones internas del administrador al revisar el reporte.';
