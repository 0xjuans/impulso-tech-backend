-- =============================================================================
-- V32 — Certificados de finalización de curso (RF-047).
--
-- Se emite un certificado cuando el estudiante completa un curso cuyo campo
-- `generates_certificate` está en TRUE. La emisión es idempotente por la
-- restricción única (user_id, course_id): el mismo estudiante nunca recibe
-- dos certificados del mismo curso.
--
-- El `verification_code` es un UUID público que permite verificar la validez
-- del certificado sin conocer los identificadores internos. Se expone en la
-- URL pública que aparece impresa en el PDF.
-- =============================================================================

CREATE TABLE certificates (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id         BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    verification_code UUID NOT NULL,
    issued_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_certificates_user_course UNIQUE (user_id, course_id),
    CONSTRAINT uk_certificates_verification UNIQUE (verification_code)
);

CREATE INDEX ix_certificates_user ON certificates (user_id);
CREATE INDEX ix_certificates_course ON certificates (course_id);

COMMENT ON TABLE certificates IS 'Certificados de finalización de curso emitidos a los estudiantes.';
COMMENT ON COLUMN certificates.verification_code IS 'Código público (UUID) impreso en el certificado y usado para verificar su validez.';
