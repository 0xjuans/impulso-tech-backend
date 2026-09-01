-- =============================================================================
-- V20 — Proyectos de programación (RF-023 / RF-045).
--
-- Un proyecto es una plantilla creada por el instructor: define el enunciado,
-- los requisitos, los criterios de evaluación y las tecnologías esperadas.
-- El estudiante realiza una o más entregas (submissions) que el instructor
-- revisa manualmente, otorgando calificación y retroalimentación.
--
-- Los proyectos son individuales: cada entrega pertenece a un único
-- estudiante y se identifica de forma independiente.
-- =============================================================================

CREATE TABLE projects (
    id                   BIGSERIAL PRIMARY KEY,
    name                 VARCHAR(180) NOT NULL,
    description          TEXT         NOT NULL,
    objective            TEXT,
    instructions         TEXT,
    requirements         TEXT,
    difficulty           VARCHAR(20)  NOT NULL,
    technologies         VARCHAR(300),
    resources            TEXT,
    evaluation_criteria  TEXT,
    max_score            INTEGER      NOT NULL DEFAULT 100,
    xp_reward            INTEGER      NOT NULL DEFAULT 100,
    deadline_at          TIMESTAMPTZ,
    status               VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    learning_route_id    BIGINT,
    course_id            BIGINT,
    module_id            BIGINT,
    instructor_id        BIGINT       NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_projects_instructor     FOREIGN KEY (instructor_id)     REFERENCES users            (id),
    CONSTRAINT fk_projects_learning_route FOREIGN KEY (learning_route_id) REFERENCES learning_routes  (id) ON DELETE SET NULL,
    CONSTRAINT fk_projects_course         FOREIGN KEY (course_id)         REFERENCES courses          (id) ON DELETE SET NULL,
    CONSTRAINT fk_projects_module         FOREIGN KEY (module_id)         REFERENCES course_modules   (id) ON DELETE SET NULL,
    CONSTRAINT ck_projects_difficulty     CHECK (difficulty IN ('PRINCIPIANTE', 'INTERMEDIO', 'AVANZADO')),
    CONSTRAINT ck_projects_status         CHECK (status IN ('BORRADOR', 'PUBLICADO', 'DESHABILITADO')),
    CONSTRAINT ck_projects_score          CHECK (max_score > 0),
    CONSTRAINT ck_projects_xp             CHECK (xp_reward >= 0)
);

CREATE INDEX ix_projects_status         ON projects (status);
CREATE INDEX ix_projects_instructor     ON projects (instructor_id);
CREATE INDEX ix_projects_course         ON projects (course_id);
CREATE INDEX ix_projects_module         ON projects (module_id);
CREATE INDEX ix_projects_learning_route ON projects (learning_route_id);

COMMENT ON TABLE  projects                     IS 'Proyectos de programación individuales publicados por instructores.';
COMMENT ON COLUMN projects.deadline_at         IS 'Fecha límite opcional para entregar el proyecto.';
COMMENT ON COLUMN projects.xp_reward           IS 'XP otorgada al estudiante la primera vez que su entrega sea aprobada.';
COMMENT ON COLUMN projects.requirements        IS 'Requisitos funcionales que debe cumplir la solución.';
COMMENT ON COLUMN projects.evaluation_criteria IS 'Criterios utilizados por el instructor al evaluar la entrega.';

CREATE TABLE project_submissions (
    id                 BIGSERIAL PRIMARY KEY,
    project_id         BIGINT       NOT NULL,
    user_id            BIGINT       NOT NULL,
    submission_number  INTEGER      NOT NULL,
    submission_url     VARCHAR(1000) NOT NULL,
    student_notes      TEXT,
    status             VARCHAR(30)  NOT NULL DEFAULT 'ENVIADA',
    grade              INTEGER,
    feedback           TEXT,
    late_submission    BOOLEAN      NOT NULL DEFAULT FALSE,
    reviewed_by        BIGINT,
    reviewed_at        TIMESTAMPTZ,
    submitted_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_project_submissions_project  FOREIGN KEY (project_id)  REFERENCES projects (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_submissions_user     FOREIGN KEY (user_id)     REFERENCES users    (id) ON DELETE CASCADE,
    CONSTRAINT fk_project_submissions_reviewer FOREIGN KEY (reviewed_by) REFERENCES users    (id) ON DELETE SET NULL,
    CONSTRAINT ck_project_submissions_status CHECK (status IN ('ENVIADA', 'EN_REVISION', 'APROBADA', 'CORRECCION_SOLICITADA', 'RECHAZADA')),
    CONSTRAINT ck_project_submissions_grade  CHECK (grade IS NULL OR (grade >= 0 AND grade <= 100)),
    CONSTRAINT uk_project_submissions_user_number UNIQUE (project_id, user_id, submission_number)
);

CREATE INDEX ix_project_submissions_project      ON project_submissions (project_id);
CREATE INDEX ix_project_submissions_user_project ON project_submissions (user_id, project_id);
CREATE INDEX ix_project_submissions_status       ON project_submissions (status);

COMMENT ON TABLE  project_submissions                 IS 'Entregas realizadas por los estudiantes sobre los proyectos publicados.';
COMMENT ON COLUMN project_submissions.submission_url  IS 'URL con el trabajo entregado (repositorio, documento o archivo).';
COMMENT ON COLUMN project_submissions.late_submission IS 'Indica si la entrega se realizó después de la fecha límite.';
COMMENT ON COLUMN project_submissions.status          IS 'Estado del ciclo de vida de la entrega desde el envío hasta la calificación final.';
