-- =============================================================================
-- V34 — Firma del instructor para certificados (RF-047).
--
-- Cada instructor puede cargar o dibujar su firma. Se persiste como
-- data URL en formato PNG o JPEG (base64) para no requerir integrar
-- almacenamiento de archivos adicional. El campo es opcional y sólo
-- aplica a usuarios con rol INSTRUCTOR o ADMINISTRADOR; los estudiantes
-- pueden tenerlo NULL siempre.
-- =============================================================================

ALTER TABLE users
    ADD COLUMN signature_image_url TEXT;

COMMENT ON COLUMN users.signature_image_url IS
    'Firma del instructor codificada como data URL base64 (image/png o image/jpeg). Se dibuja sobre la línea de firma de los certificados emitidos para sus cursos.';
