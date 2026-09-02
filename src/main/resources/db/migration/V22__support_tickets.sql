-- =============================================================================
-- V22 — Soporte y reporte de problemas (RF-036).
--
-- Los usuarios reportan problemas de la plataforma o de contenido educativo.
-- Cada ticket puede vincularse opcionalmente a un recurso concreto y ser
-- asignado a un instructor o administrador para su seguimiento hasta la
-- resolución. El histórico se conserva mediante los campos de auditoría y
-- las notas de resolución del responsable.
-- =============================================================================

CREATE TABLE support_tickets (
    id                BIGSERIAL PRIMARY KEY,
    reporter_id       BIGINT       NOT NULL,
    type              VARCHAR(40)  NOT NULL,
    title             VARCHAR(200) NOT NULL,
    description       TEXT         NOT NULL,
    related_id        BIGINT,
    status            VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    assignee_id       BIGINT,
    resolution_notes  TEXT,
    resolved_at       TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_support_tickets_reporter FOREIGN KEY (reporter_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_support_tickets_assignee FOREIGN KEY (assignee_id) REFERENCES users (id) ON DELETE SET NULL,
    CONSTRAINT ck_support_tickets_type   CHECK (type IN (
        'TECHNICAL_ERROR', 'COURSE_ISSUE', 'LESSON_ISSUE', 'ACTIVITY_ISSUE',
        'CHALLENGE_ISSUE', 'LAB_ISSUE', 'EVALUATION_ISSUE', 'PROJECT_ISSUE',
        'RESOURCE_ISSUE', 'AI_MASCOT_ISSUE', 'OTHER'
    )),
    CONSTRAINT ck_support_tickets_status CHECK (status IN (
        'PENDIENTE', 'EN_REVISION', 'EN_PROCESO', 'RESUELTO', 'CERRADO'
    ))
);

CREATE INDEX ix_support_tickets_reporter ON support_tickets (reporter_id);
CREATE INDEX ix_support_tickets_assignee ON support_tickets (assignee_id);
CREATE INDEX ix_support_tickets_status   ON support_tickets (status);
CREATE INDEX ix_support_tickets_type     ON support_tickets (type);
CREATE INDEX ix_support_tickets_created  ON support_tickets (created_at DESC);

COMMENT ON TABLE  support_tickets                  IS 'Tickets de soporte y reportes de problemas registrados por los usuarios.';
COMMENT ON COLUMN support_tickets.type             IS 'Tipo de problema (tipo del ticket). Cuando aplica, indica el tipo del recurso relacionado.';
COMMENT ON COLUMN support_tickets.related_id       IS 'Identificador del recurso relacionado, cuando corresponda al tipo del ticket.';
COMMENT ON COLUMN support_tickets.assignee_id      IS 'Responsable actual del ticket (instructor o administrador). Nulo cuando aún no ha sido asignado.';
COMMENT ON COLUMN support_tickets.resolution_notes IS 'Notas de resolución registradas por el responsable.';
