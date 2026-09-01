-- =============================================================================
-- V12 — Insignias y logros (RF-019 / RF-046).
--
-- Cada insignia se otorga automáticamente cuando el estudiante alcanza una
-- condición definida por su tipo de disparador (por ejemplo, completar N
-- lecciones, alcanzar cierto nivel, mantener una racha determinada, etc.).
--
-- Se incluyen insignias predeterminadas para cubrir los hitos iniciales de
-- aprendizaje y gamificación de la plataforma. Los administradores podrán
-- añadir, deshabilitar o ajustar insignias adicionales desde la aplicación
-- en versiones posteriores.
-- =============================================================================

CREATE TABLE badges (
    id             BIGSERIAL PRIMARY KEY,
    code           VARCHAR(60)  NOT NULL,
    name           VARCHAR(120) NOT NULL,
    description    TEXT         NOT NULL,
    icon_url       VARCHAR(500),
    category       VARCHAR(30)  NOT NULL,
    rarity         VARCHAR(20)  NOT NULL DEFAULT 'COMUN',
    trigger_type   VARCHAR(40)  NOT NULL,
    trigger_value  INTEGER      NOT NULL DEFAULT 0,
    active         BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_badges_code       UNIQUE (code),
    CONSTRAINT ck_badges_category   CHECK (category IN ('PROGRESO', 'GAMIFICACION', 'ESPECIAL')),
    CONSTRAINT ck_badges_rarity     CHECK (rarity IN ('COMUN', 'RARA', 'EPICA', 'LEGENDARIA')),
    CONSTRAINT ck_badges_trigger    CHECK (trigger_type IN (
        'LESSONS_COMPLETED_COUNT',
        'COURSES_COMPLETED_COUNT',
        'STREAK_REACHED',
        'XP_REACHED',
        'LEVEL_REACHED'
    )),
    CONSTRAINT ck_badges_trig_value CHECK (trigger_value >= 0)
);

CREATE INDEX ix_badges_trigger ON badges (trigger_type);

COMMENT ON TABLE  badges                IS 'Catálogo de insignias otorgables automáticamente en Impulso Tech.';
COMMENT ON COLUMN badges.code           IS 'Identificador estable de la insignia; se conserva aunque cambie el nombre visible.';
COMMENT ON COLUMN badges.trigger_type   IS 'Condición que dispara el otorgamiento (lecciones completadas, racha, XP, etc.).';
COMMENT ON COLUMN badges.trigger_value  IS 'Umbral asociado al disparador (por ejemplo, cantidad de lecciones).';

CREATE TABLE user_badges (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    badge_id    BIGINT       NOT NULL,
    awarded_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_user_badges_user  FOREIGN KEY (user_id)  REFERENCES users  (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_badges_badge FOREIGN KEY (badge_id) REFERENCES badges (id) ON DELETE CASCADE,
    CONSTRAINT uk_user_badges_user_badge UNIQUE (user_id, badge_id)
);

CREATE INDEX ix_user_badges_user ON user_badges (user_id);

COMMENT ON TABLE user_badges IS 'Insignias otorgadas a cada usuario; la restricción única impide duplicados.';

-- Insignias predeterminadas.
INSERT INTO badges (code, name, description, category, rarity, trigger_type, trigger_value) VALUES
    ('LESSON_FIRST',       'Primeros pasos',    'Completaste tu primera lección en Impulso Tech.',        'PROGRESO',     'COMUN',      'LESSONS_COMPLETED_COUNT',  1),
    ('LESSON_TEN',         'Estudiante constante', 'Completaste 10 lecciones. ¡Sigue así!',                'PROGRESO',     'COMUN',      'LESSONS_COMPLETED_COUNT', 10),
    ('LESSON_FIFTY',       'Explorador',        'Completaste 50 lecciones dentro de la plataforma.',      'PROGRESO',     'RARA',       'LESSONS_COMPLETED_COUNT', 50),
    ('COURSE_FIRST',       'Curso completado',  'Terminaste tu primer curso en Impulso Tech.',            'PROGRESO',     'RARA',       'COURSES_COMPLETED_COUNT',  1),
    ('COURSE_FIVE',        'Coleccionista',     'Completaste 5 cursos. Un logro destacado.',              'PROGRESO',     'EPICA',      'COURSES_COMPLETED_COUNT',  5),
    ('STREAK_SEVEN',       'Semana perfecta',   'Mantuviste una racha de 7 días consecutivos.',           'GAMIFICACION', 'RARA',       'STREAK_REACHED',           7),
    ('STREAK_THIRTY',      'Constancia',        'Mantuviste una racha de 30 días consecutivos.',          'GAMIFICACION', 'EPICA',      'STREAK_REACHED',          30),
    ('STREAK_HUNDRED',     'Leyenda del hábito', 'Alcanzaste 100 días consecutivos de aprendizaje.',      'GAMIFICACION', 'LEGENDARIA', 'STREAK_REACHED',         100),
    ('XP_ONE_HUNDRED',     'Cien puntos',       'Alcanzaste los 100 puntos de experiencia.',              'GAMIFICACION', 'COMUN',      'XP_REACHED',             100),
    ('XP_ONE_THOUSAND',    'Mil puntos',        'Alcanzaste los 1000 puntos de experiencia.',             'GAMIFICACION', 'RARA',       'XP_REACHED',            1000),
    ('LEVEL_FIVE',         'Nivel 5',           'Alcanzaste el nivel 5 en Impulso Tech.',                 'GAMIFICACION', 'COMUN',      'LEVEL_REACHED',            5),
    ('LEVEL_TEN',          'Nivel 10',          'Alcanzaste el nivel 10 en Impulso Tech.',                'GAMIFICACION', 'EPICA',      'LEVEL_REACHED',           10);
