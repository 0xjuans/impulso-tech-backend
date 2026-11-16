-- =============================================================================
-- V35 — Limpieza completa de contenido educativo y datos de prueba.
--
-- Elimina todos los registros sembrados por V30 y V31 (y cualquier otro
-- dato de contenido que exista), dejando la base de datos únicamente con
-- la estructura de tablas y el usuario administrador que AdminBootstrap
-- crea al arrancar la aplicación.
--
-- Se utiliza TRUNCATE ... RESTART IDENTITY CASCADE para:
--   * Borrar todas las filas de las tablas indicadas.
--   * Propagar la limpieza a las tablas dependientes (CASCADE).
--   * Reiniciar las secuencias de identidad a 1.
-- =============================================================================

TRUNCATE TABLE
    -- Contenido educativo principal
    learning_routes,
    courses,

    -- Contenido asociado a cursos
    challenges,
    projects,
    labs,
    educational_resources,
    evaluations,
    activities,

    -- Comunidad
    community_posts,

    -- Progreso y gamificación
    enrollments,
    certificates,
    xp_events,
    user_xp,
    user_badges,
    badges,
    user_streaks,

    -- IA y mascota
    ai_conversations,
    mascot_customization,
    mascot_unlocks,
    mascot_items,

    -- Soporte y administración
    notifications,
    admin_activity_log,
    support_tickets,
    stored_files,

    -- Mensajería
    message_conversations

RESTART IDENTITY CASCADE;
