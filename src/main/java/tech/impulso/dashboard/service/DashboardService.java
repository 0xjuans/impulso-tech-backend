package tech.impulso.dashboard.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.challenges.repository.ChallengeAttemptRepository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.dashboard.dto.AdminDashboardResponse;
import tech.impulso.dashboard.dto.InstructorDashboardResponse;
import tech.impulso.enrollments.repository.EnrollmentRepository;
import tech.impulso.learningroutes.repository.LearningRouteRepository;
import tech.impulso.lessons.repository.LessonRepository;
import tech.impulso.projects.repository.ProjectSubmissionRepository;
import tech.impulso.support.entity.SupportTicketStatus;
import tech.impulso.support.repository.SupportTicketRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;
import tech.impulso.users.repository.UserRepository;

/**
 * Servicio que expone las métricas consolidadas de los paneles de
 * instructor (RF-032) y administrador (RF-033).
 *
 * <p>Las métricas se calculan bajo demanda mediante conteos directos en
 * la base de datos, evitando materializaciones intermedias para
 * garantizar que los valores reflejen siempre el estado actual.</p>
 */
@Service
public class DashboardService {

    private final CourseRepository courseRepository;
    private final LearningRouteRepository routeRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final ProjectSubmissionRepository projectSubmissionRepository;
    private final SupportTicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public DashboardService(CourseRepository courseRepository,
                            LearningRouteRepository routeRepository,
                            LessonRepository lessonRepository,
                            EnrollmentRepository enrollmentRepository,
                            ChallengeAttemptRepository challengeAttemptRepository,
                            ProjectSubmissionRepository projectSubmissionRepository,
                            SupportTicketRepository ticketRepository,
                            UserRepository userRepository,
                            CurrentUserService currentUserService) {
        this.courseRepository = courseRepository;
        this.routeRepository = routeRepository;
        this.lessonRepository = lessonRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.challengeAttemptRepository = challengeAttemptRepository;
        this.projectSubmissionRepository = projectSubmissionRepository;
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve las métricas del panel del instructor autenticado.
     */
    @Transactional(readOnly = true)
    public InstructorDashboardResponse getInstructorDashboard() {
        User instructor = currentUserService.requireAuthenticatedUser();
        Long id = instructor.getId();

        long routesTotal = routeRepository.countByInstructorId(id);
        long routesPublished = routeRepository.countByInstructorIdAndStatus(id, ContentStatus.PUBLICADO);
        long routesDraft = routeRepository.countByInstructorIdAndStatus(id, ContentStatus.BORRADOR);

        long coursesTotal = courseRepository.countByInstructorId(id);
        long coursesPublished = courseRepository.countByInstructorIdAndStatus(id, ContentStatus.PUBLICADO);
        long coursesDraft = courseRepository.countByInstructorIdAndStatus(id, ContentStatus.BORRADOR);
        long coursesDisabled = courseRepository.countByInstructorIdAndStatus(id, ContentStatus.DESHABILITADO);

        long publishedLessons = lessonRepository.countPublishedByInstructor(id);
        long totalEnrollments = enrollmentRepository.countByInstructor(id);
        long uniqueStudents = enrollmentRepository.countDistinctStudentsByInstructor(id);
        long pendingChallenges = challengeAttemptRepository.countPendingByInstructor(id);
        long pendingProjects = projectSubmissionRepository.countPendingByInstructor(id);

        long openAssignedTickets =
                  ticketRepository.countByAssigneeIdAndStatus(id, SupportTicketStatus.PENDIENTE)
                + ticketRepository.countByAssigneeIdAndStatus(id, SupportTicketStatus.EN_REVISION)
                + ticketRepository.countByAssigneeIdAndStatus(id, SupportTicketStatus.EN_PROCESO);

        return new InstructorDashboardResponse(
                routesTotal, routesPublished, routesDraft,
                coursesTotal, coursesPublished, coursesDraft, coursesDisabled,
                publishedLessons, totalEnrollments, uniqueStudents,
                pendingChallenges, pendingProjects, openAssignedTickets
        );
    }

    /**
     * Devuelve las métricas globales del panel del administrador.
     */
    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        long students = userRepository.countByRole(Role.ESTUDIANTE);
        long instructors = userRepository.countByRole(Role.INSTRUCTOR);
        long administrators = userRepository.countByRole(Role.ADMINISTRADOR);

        long activeUsers = userRepository.countByStatus(UserStatus.ACTIVA);
        long pendingUsers = userRepository.countByStatus(UserStatus.PENDIENTE_VERIFICACION);
        long disabledUsers = userRepository.countByStatus(UserStatus.DESACTIVADA);

        long routesTotal = routeRepository.count();
        long routesPublished = routeRepository.countByStatus(ContentStatus.PUBLICADO);

        long coursesTotal = courseRepository.count();
        long coursesPublished = courseRepository.countByStatus(ContentStatus.PUBLICADO);
        long coursesDraft = courseRepository.countByStatus(ContentStatus.BORRADOR);
        long coursesDisabled = courseRepository.countByStatus(ContentStatus.DESHABILITADO);

        long publishedLessons = lessonRepository.countAllPublished();
        long totalEnrollments = enrollmentRepository.count();

        long openTickets =
                  ticketRepository.countByStatus(SupportTicketStatus.PENDIENTE)
                + ticketRepository.countByStatus(SupportTicketStatus.EN_REVISION)
                + ticketRepository.countByStatus(SupportTicketStatus.EN_PROCESO);
        long resolvedTickets = ticketRepository.countByStatus(SupportTicketStatus.RESUELTO);
        long closedTickets = ticketRepository.countByStatus(SupportTicketStatus.CERRADO);

        return new AdminDashboardResponse(
                students, instructors, administrators,
                activeUsers, pendingUsers, disabledUsers,
                routesTotal, routesPublished,
                coursesTotal, coursesPublished, coursesDraft, coursesDisabled,
                publishedLessons, totalEnrollments,
                openTickets, resolvedTickets, closedTickets
        );
    }
}
