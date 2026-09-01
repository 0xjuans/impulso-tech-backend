package tech.impulso.gamification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.enrollments.entity.EnrollmentStatus;
import tech.impulso.enrollments.repository.EnrollmentRepository;
import tech.impulso.enrollments.repository.LessonCompletionRepository;
import tech.impulso.gamification.entity.Badge;
import tech.impulso.gamification.entity.BadgeTrigger;
import tech.impulso.gamification.entity.UserBadge;
import tech.impulso.gamification.entity.UserStreak;
import tech.impulso.gamification.entity.UserXp;
import tech.impulso.gamification.repository.BadgeRepository;
import tech.impulso.gamification.repository.UserBadgeRepository;
import tech.impulso.gamification.repository.UserStreakRepository;
import tech.impulso.gamification.repository.UserXpRepository;
import tech.impulso.users.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Servicio responsable de evaluar y otorgar insignias a los estudiantes
 * (RF-019 / RF-046).
 *
 * <p>El otorgamiento es siempre automático: al ocurrir un evento
 * relevante (por ejemplo, completar una lección) se invoca
 * {@link #evaluateAndAward(User)}, que revisa todas las condiciones y
 * añade al usuario las insignias que aún no posee y cuyo umbral se
 * cumpla.</p>
 *
 * <p>La restricción única {@code (user_id, badge_id)} en base de datos y
 * el chequeo previo por identificador garantizan que ninguna insignia se
 * otorgue dos veces al mismo estudiante.</p>
 */
@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;
    private final LessonCompletionRepository lessonCompletionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserXpRepository userXpRepository;
    private final UserStreakRepository userStreakRepository;

    public BadgeService(BadgeRepository badgeRepository,
                        UserBadgeRepository userBadgeRepository,
                        LessonCompletionRepository lessonCompletionRepository,
                        EnrollmentRepository enrollmentRepository,
                        UserXpRepository userXpRepository,
                        UserStreakRepository userStreakRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
        this.lessonCompletionRepository = lessonCompletionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.userXpRepository = userXpRepository;
        this.userStreakRepository = userStreakRepository;
    }

    /**
     * Evalúa todas las insignias activas y otorga al usuario las que
     * cumpla y todavía no posea.
     *
     * @param user usuario a evaluar.
     * @return insignias otorgadas durante esta invocación.
     */
    @Transactional
    public List<UserBadge> evaluateAndAward(User user) {
        Set<Long> alreadyOwned = userBadgeRepository.findBadgeIdsByUserId(user.getId());
        List<Badge> candidates = badgeRepository.findByActiveTrueOrderByRarityAscNameAsc().stream()
                .filter(badge -> !alreadyOwned.contains(badge.getId()))
                .toList();

        if (candidates.isEmpty()) {
            return List.of();
        }

        UserMetrics metrics = collectMetrics(user);
        List<UserBadge> awarded = new ArrayList<>();
        for (Badge badge : candidates) {
            if (meetsCondition(badge, metrics)) {
                UserBadge userBadge = new UserBadge();
                userBadge.setUser(user);
                userBadge.setBadge(badge);
                awarded.add(userBadgeRepository.save(userBadge));
            }
        }
        return awarded;
    }

    /**
     * Consulta las insignias obtenidas por el usuario indicado.
     *
     * @param userId identificador del usuario.
     * @return insignias otorgadas, más recientes primero.
     */
    @Transactional(readOnly = true)
    public List<UserBadge> listUserBadges(Long userId) {
        return userBadgeRepository.findByUserIdOrderByAwardedAtDesc(userId);
    }

    /**
     * Devuelve el catálogo de insignias activas de la plataforma.
     *
     * @return insignias activas.
     */
    @Transactional(readOnly = true)
    public List<Badge> listCatalog() {
        return badgeRepository.findByActiveTrueOrderByRarityAscNameAsc();
    }

    /**
     * Determina si una insignia cumple su condición con las métricas
     * actuales del usuario.
     *
     * @param badge   insignia a evaluar.
     * @param metrics métricas actuales del usuario.
     * @return {@code true} cuando el umbral está alcanzado.
     */
    private boolean meetsCondition(Badge badge, UserMetrics metrics) {
        int threshold = badge.getTriggerValue();
        return switch (badge.getTriggerType()) {
            case LESSONS_COMPLETED_COUNT -> metrics.lessonsCompleted >= threshold;
            case COURSES_COMPLETED_COUNT -> metrics.coursesCompleted >= threshold;
            case STREAK_REACHED          -> metrics.longestStreak    >= threshold;
            case XP_REACHED              -> metrics.totalXp          >= threshold;
            case LEVEL_REACHED           -> metrics.currentLevel     >= threshold;
        };
    }

    /**
     * Recopila las métricas actuales del usuario para poder evaluar las
     * insignias en una sola pasada.
     *
     * @param user usuario evaluado.
     * @return métricas agregadas.
     */
    private UserMetrics collectMetrics(User user) {
        long lessonsCompleted = lessonCompletionRepository.countByUserId(user.getId());
        long coursesCompleted = enrollmentRepository.countByUserIdAndStatus(user.getId(), EnrollmentStatus.COMPLETADO);
        UserXp xp = userXpRepository.findById(user.getId()).orElse(null);
        UserStreak streak = userStreakRepository.findById(user.getId()).orElse(null);

        int totalXp = xp == null ? 0 : xp.getTotalXp();
        int currentLevel = xp == null ? 1 : xp.getCurrentLevel();
        int longestStreak = streak == null ? 0 : streak.getLongestStreak();

        return new UserMetrics(lessonsCompleted, coursesCompleted, longestStreak, totalXp, currentLevel);
    }

    /**
     * Vista consolidada de las métricas relevantes para la evaluación
     * de insignias.
     */
    private record UserMetrics(
            long lessonsCompleted,
            long coursesCompleted,
            int longestStreak,
            int totalXp,
            int currentLevel
    ) {
    }
}
