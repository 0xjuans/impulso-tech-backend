-- =============================================================================
-- V2 — Módulo de usuarios y autenticación.
--
-- Crea la tabla principal de usuarios y las tablas auxiliares necesarias para
-- la verificación de correo electrónico y la recuperación de contraseña.
--
-- Reglas aplicadas:
--   - El correo electrónico y el nombre de usuario son únicos por cuenta.
--   - Las contraseñas se almacenan cifradas con BCrypt (nunca en texto plano).
--   - Los tokens de verificación y recuperación son de un solo uso y con
--     tiempo de expiración.
--   - Los timestamps se almacenan en UTC.
-- =============================================================================

-- Tabla principal de usuarios de la plataforma.
CREATE TABLE users (
    id                   BIGSERIAL PRIMARY KEY,
    email                VARCHAR(255) NOT NULL,
    username             VARCHAR(60)  NOT NULL,
    password_hash        VARCHAR(255),
    first_name           VARCHAR(80)  NOT NULL,
    last_name            VARCHAR(80)  NOT NULL,
    profile_photo_url    VARCHAR(500),
    role                 VARCHAR(30)  NOT NULL,
    status               VARCHAR(30)  NOT NULL,
    email_verified_at    TIMESTAMPTZ,
    last_login_at        TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_users_email    UNIQUE (email),
    CONSTRAINT uk_users_username UNIQUE (username),
    CONSTRAINT ck_users_role     CHECK (role   IN ('ESTUDIANTE', 'INSTRUCTOR', 'ADMINISTRADOR')),
    CONSTRAINT ck_users_status   CHECK (status IN ('PENDIENTE_VERIFICACION', 'ACTIVA', 'DESACTIVADA'))
);

COMMENT ON TABLE  users              IS 'Usuarios registrados en Impulso Tech.';
COMMENT ON COLUMN users.password_hash IS 'Hash BCrypt de la contraseña. Nulo cuando el usuario se autentica exclusivamente mediante proveedores externos como Google OAuth.';
COMMENT ON COLUMN users.role         IS 'Rol funcional del usuario en la plataforma.';
COMMENT ON COLUMN users.status       IS 'Estado del ciclo de vida de la cuenta.';

-- Tokens de verificación de correo electrónico (RF-001).
CREATE TABLE email_verification_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_email_verification_token UNIQUE (token)
);

CREATE INDEX ix_email_verification_user_id ON email_verification_tokens (user_id);

COMMENT ON TABLE email_verification_tokens IS 'Tokens de un solo uso enviados al correo del usuario para confirmar su cuenta.';

-- Tokens de recuperación de contraseña (RF-003 / RF-054).
CREATE TABLE password_reset_tokens (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    token       VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMPTZ  NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_password_reset_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uk_password_reset_token UNIQUE (token)
);

CREATE INDEX ix_password_reset_user_id ON password_reset_tokens (user_id);

COMMENT ON TABLE password_reset_tokens IS 'Tokens de un solo uso enviados al correo para permitir el restablecimiento de la contraseña.';
