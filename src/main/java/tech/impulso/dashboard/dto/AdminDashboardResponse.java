package tech.impulso.dashboard.dto;

/**
 * Métricas globales del panel de administrador (RF-033).
 *
 * <p>Presenta una visión general del estado de la plataforma: usuarios
 * por rol y estado, contenidos por estado, actividad y tickets abiertos.
 * Todos los conteos se calculan al momento de la consulta.</p>
 *
 * @param students          usuarios con rol {@code ESTUDIANTE}.
 * @param instructors       usuarios con rol {@code INSTRUCTOR}.
 * @param administrators    usuarios con rol {@code ADMINISTRADOR}.
 * @param activeUsers       usuarios en estado {@code ACTIVA}.
 * @param pendingUsers      usuarios pendientes de verificar correo.
 * @param disabledUsers     usuarios desactivados por el administrador.
 * @param routesTotal       total de rutas de aprendizaje.
 * @param routesPublished   rutas publicadas.
 * @param coursesTotal      total de cursos.
 * @param coursesPublished  cursos publicados.
 * @param coursesDraft      cursos en borrador.
 * @param coursesDisabled   cursos deshabilitados.
 * @param publishedLessons  lecciones publicadas en toda la plataforma.
 * @param totalEnrollments  total de inscripciones registradas.
 * @param openTickets       tickets abiertos (pendientes, en revisión, en proceso).
 * @param resolvedTickets   tickets resueltos.
 * @param closedTickets     tickets cerrados.
 */
public record AdminDashboardResponse(
        long students,
        long instructors,
        long administrators,
        long activeUsers,
        long pendingUsers,
        long disabledUsers,
        long routesTotal,
        long routesPublished,
        long coursesTotal,
        long coursesPublished,
        long coursesDraft,
        long coursesDisabled,
        long publishedLessons,
        long totalEnrollments,
        long openTickets,
        long resolvedTickets,
        long closedTickets
) {
}
