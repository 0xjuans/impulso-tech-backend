package tech.impulso.progress.service;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.challenges.repository.ChallengeAttemptRepository;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.enrollments.entity.EnrollmentStatus;
import tech.impulso.enrollments.repository.EnrollmentRepository;
import tech.impulso.enrollments.repository.LessonCompletionRepository;
import tech.impulso.gamification.entity.UserStreak;
import tech.impulso.gamification.entity.UserXp;
import tech.impulso.gamification.entity.XpEvent;
import tech.impulso.gamification.repository.UserBadgeRepository;
import tech.impulso.gamification.repository.XpEventRepository;
import tech.impulso.gamification.service.StreakService;
import tech.impulso.gamification.service.XpService;
import tech.impulso.notifications.repository.NotificationRepository;
import tech.impulso.progress.dto.ActivityHistoryEntryResponse;
import tech.impulso.progress.dto.StudentDashboardResponse;
import tech.impulso.projects.repository.ProjectSubmissionRepository;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Servicio consolidado que expone el historial de actividad (RF-028) y
 * el panel de progreso (RF-029) del estudiante autenticado.
 *
 * <p>Las consultas se apoyan en los agregados y eventos ya registrados
 * por los módulos existentes; no persiste información adicional.</p>
 */
@Service
public class ProgressService {

    private final CurrentUserService currentUserService;
    private final XpService xpService;
    private final StreakService streakService;
    private final EnrollmentRepository enrollmentRepository;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final ChallengeAttemptRepository challengeAttemptRepository;
    private final ProjectSubmissionRepository projectSubmissionRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final NotificationRepository notificationRepository;
    private final XpEventRepository xpEventRepository;

    public ProgressService(CurrentUserService currentUserService,
                           XpService xpService,
                           StreakService streakService,
                           EnrollmentRepository enrollmentRepository,
                           LessonCompletionRepository lessonCompletionRepository,
                           ChallengeAttemptRepository challengeAttemptRepository,
                           ProjectSubmissionRepository projectSubmissionRepository,
                           UserBadgeRepository userBadgeRepository,
                           NotificationRepository notificationRepository,
                           XpEventRepository xpEventRepository) {
        this.currentUserService = currentUserService;
        this.xpService = xpService;
        this.streakService = streakService;
        this.enrollmentRepository = enrollmentRepository;
        this.lessonCompletionRepository = lessonCompletionRepository;
        this.challengeAttemptRepository = challengeAttemptRepository;
        this.projectSubmissionRepository = projectSubmissionRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.notificationRepository = notificationRepository;
        this.xpEventRepository = xpEventRepository;
    }

    /**
     * Construye el panel de progreso del usuario autenticado.
     */
    @Transactional
    public StudentDashboardResponse buildDashboard() {
        User user = currentUserService.requireAuthenticatedUser();

        UserXp xp = xpService.findOrCreate(user);
        UserStreak streak = streakService.findOrCreate(user);

        long enrolled = enrollmentRepository.countByUserId(user.getId());
        long completed = enrollmentRepository.countByUserIdAndStatus(user.getId(), EnrollmentStatus.COMPLETADO);
        long lessons = lessonCompletionRepository.countByUserId(user.getId());
        long challenges = challengeAttemptRepository.countDistinctSolvedChallenges(user.getId());
        long projects = projectSubmissionRepository.countDistinctApprovedProjects(user.getId());
        long badges = (long) userBadgeRepository.findBadgeIdsByUserId(user.getId()).size();
        long unreadNotifications = notificationRepository.countUnread(user.getId());

        int xpForCurrent = xpService.xpRequiredForLevel(xp.getCurrentLevel());
        int xpForNext = xpService.xpRequiredForLevel(xp.getCurrentLevel() + 1);
        int xpToNext = Math.max(0, xpForNext - xp.getTotalXp());

        return new StudentDashboardResponse(
                xp.getTotalXp(),
                xp.getCurrentLevel(),
                xpForCurrent,
                xpForNext,
                xpToNext,
                streak.getCurrentStreak(),
                streak.getLongestStreak(),
                streak.getStreakStartedOn(),
                streak.getLastActivityDate(),
                enrolled,
                completed,
                lessons,
                challenges,
                projects,
                badges,
                unreadNotifications,
                OffsetDateTime.now(ZoneOffset.UTC)
        );
    }

    /**
     * Devuelve una página del historial de actividad del usuario
     * autenticado ordenado cronológicamente descendente.
     *
     * <p>La fuente principal son los eventos de XP registrados por la
     * plataforma (que ya cubren lecciones, cursos, actividades, retos y
     * proyectos). Se complementan con las insignias obtenidas para
     * ofrecer una línea de tiempo completa. Para MVP se combina en
     * memoria; cuando el volumen lo justifique se podrá migrar a una
     * consulta SQL unificada.</p>
     */
    @Transactional(readOnly = true)
    public PagedResponse<ActivityHistoryEntryResponse> listMyHistory(Pageable pageable) {
        User user = currentUserService.requireAuthenticatedUser();

        List<ActivityHistoryEntryResponse> entries = new ArrayList<>();

        xpEventRepository.findByUserId(user.getId(), Pageable.unpaged()).forEach(event ->
                entries.add(fromXpEvent(event)));

        userBadgeRepository.findByUserIdOrderByAwardedAtDesc(user.getId()).forEach(userBadge ->
                entries.add(new ActivityHistoryEntryResponse(
                        "BADGE_AWARDED",
                        userBadge.getBadge().getCode(),
                        "Nueva insignia: %s".formatted(userBadge.getBadge().getName()),
                        userBadge.getBadge().getDescription(),
                        null,
                        userBadge.getBadge().getId(),
                        userBadge.getAwardedAt()
                )));

        entries.sort(Comparator.comparing(ActivityHistoryEntryResponse::createdAt).reversed());

        int from = (int) pageable.getOffset();
        int to = Math.min(from + pageable.getPageSize(), entries.size());
        List<ActivityHistoryEntryResponse> pageContent = from >= entries.size()
                ? List.of()
                : entries.subList(from, to);
        return PagedResponse.from(new PageImpl<>(pageContent, pageable, entries.size()), e -> e);
    }

    /**
     * Convierte un evento de XP en una entrada del historial con un
     * título legible según el tipo de fuente.
     */
    private ActivityHistoryEntryResponse fromXpEvent(XpEvent event) {
        String title = switch (event.getSourceType()) {
            case LESSON_COMPLETED   -> "Lección completada";
            case COURSE_COMPLETED   -> "Curso completado";
            case ACTIVITY_COMPLETED -> "Actividad resuelta";
            case CHALLENGE_SOLVED   -> "Reto resuelto";
            case PROJECT_APPROVED   -> "Proyecto aprobado";
        };
        String description = "Recibiste %d XP".formatted(event.getXpAwarded());
        return new ActivityHistoryEntryResponse(
                "XP_EVENT",
                event.getSourceType().name(),
                title,
                description,
                event.getXpAwarded(),
                event.getSourceId(),
                event.getCreatedAt()
        );
    }
}
