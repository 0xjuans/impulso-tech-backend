package tech.impulso.dashboard.dto;

/**
 * Métricas consolidadas del panel de instructor (RF-032).
 *
 * <p>Reúne, por instructor autenticado, el conteo de contenidos que
 * gestiona, la cantidad total de estudiantes vinculados a sus cursos y
 * las tareas que requieren su atención (revisiones y tickets).</p>
 *
 * @param routesTotal         cantidad total de rutas gestionadas.
 * @param routesPublished     rutas en estado {@code PUBLICADO}.
 * @param routesDraft         rutas en estado {@code BORRADOR}.
 * @param coursesTotal        cantidad total de cursos gestionados.
 * @param coursesPublished    cursos en estado {@code PUBLICADO}.
 * @param coursesDraft        cursos en estado {@code BORRADOR}.
 * @param coursesDisabled     cursos en estado {@code DESHABILITADO}.
 * @param publishedLessons    lecciones publicadas en cursos del instructor.
 * @param totalEnrollments    total de inscripciones registradas.
 * @param uniqueStudents      cantidad de estudiantes distintos inscritos.
 * @param pendingChallenges   intentos de reto pendientes de revisión.
 * @param pendingProjects     entregas de proyecto pendientes de revisión.
 * @param openAssignedTickets tickets de soporte asignados y sin cerrar.
 */
public record InstructorDashboardResponse(
        long routesTotal,
        long routesPublished,
        long routesDraft,
        long coursesTotal,
        long coursesPublished,
        long coursesDraft,
        long coursesDisabled,
        long publishedLessons,
        long totalEnrollments,
        long uniqueStudents,
        long pendingChallenges,
        long pendingProjects,
        long openAssignedTickets
) {
}
