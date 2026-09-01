-- =============================================================================
-- V1 — Línea base del esquema de Impulso Tech.
--
-- Esta migración únicamente establece la base para futuras migraciones y
-- deja registro del inicio del control de esquema con Flyway. Las entidades
-- de negocio se agregarán en migraciones posteriores, una por cada módulo
-- funcional (auth, users, courses, gamification, etc.).
-- =============================================================================

-- Habilitamos la extensión pgcrypto para poder generar UUIDs desde la base de
-- datos cuando alguna entidad futura lo requiera (por ejemplo, identificadores
-- públicos de certificados o tokens).
CREATE EXTENSION IF NOT EXISTS pgcrypto;
