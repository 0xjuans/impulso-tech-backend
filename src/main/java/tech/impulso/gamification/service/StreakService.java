package tech.impulso.gamification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.gamification.entity.UserStreak;
import tech.impulso.gamification.repository.UserStreakRepository;
import tech.impulso.notifications.entity.NotificationType;
import tech.impulso.notifications.service.NotificationService;
import tech.impulso.users.entity.User;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Set;

/**
 * Servicio responsable de mantener la racha de aprendizaje de cada
 * usuario (RF-020 / RF-048).
 *
 * <p>La racha se actualiza únicamente cuando el estudiante realiza una
 * actividad válida (por ejemplo, completar una lección). Las reglas
 * aplicadas son:</p>
 *
 * <ul>
 *   <li>Varias actividades el mismo día cuentan como un solo día.</li>
 *   <li>Actividad realizada el día siguiente al último registrado
 *       incrementa la racha.</li>
 *   <li>Si transcurre uno o más días sin actividad válida, la racha se
 *       reinicia en 1 al registrar la siguiente actividad.</li>
 *   <li>El récord histórico ({@code longestStreak}) se conserva incluso
 *       cuando la racha actual se reinicia.</li>
 * </ul>
 */
@Service
public class StreakService {

    /**
     * Hitos de racha que disparan una notificación motivacional al
     * estudiante cuando los alcanza por primera vez.
     */
    private static final Set<Integer> MILESTONES = Set.of(3, 7, 14, 30, 60, 100);

    private final UserStreakRepository repository;
    private final NotificationService notificationService;

    public StreakService(UserStreakRepository repository,
                         NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    /**
     * Registra una actividad válida realizada por el usuario en la
     * fecha actual (UTC) y actualiza su racha en consecuencia.
     *
     * @param user usuario que realizó la actividad.
     * @return racha actualizada.
     */
    @Transactional
    public UserStreak registerActivity(User user) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        UserStreak streak = repository.findById(user.getId())
                .orElseGet(() -> createInitial(user));

        LocalDate lastDate = streak.getLastActivityDate();
        if (lastDate == null) {
            streak.setCurrentStreak(1);
            streak.setStreakStartedOn(today);
        } else if (lastDate.equals(today)) {
            // Actividad adicional el mismo día: la racha no cambia.
            return streak;
        } else {
            long daysBetween = ChronoUnit.DAYS.between(lastDate, today);
            if (daysBetween == 1) {
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            } else {
                streak.setCurrentStreak(1);
                streak.setStreakStartedOn(today);
            }
        }

        streak.setLastActivityDate(today);
        int previousLongest = streak.getLongestStreak();
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        UserStreak saved = repository.save(streak);

        // Notificamos únicamente cuando el hito se alcanza por primera
        // vez, es decir, cuando la racha actual supera el récord anterior.
        if (MILESTONES.contains(saved.getCurrentStreak())
                && saved.getCurrentStreak() > previousLongest) {
            notificationService.notify(
                    user,
                    NotificationType.STREAK_MILESTONE,
                    "¡Racha de %d días!".formatted(saved.getCurrentStreak()),
                    "Alcanzaste una racha de %d días consecutivos aprendiendo. ¡Sigue así!"
                            .formatted(saved.getCurrentStreak()),
                    "USER",
                    user.getId()
            );
        }
        return saved;
    }

    /**
     * Devuelve la racha del usuario, creándola si aún no existe. Aplica
     * también la caída natural: si el último día registrado es anterior
     * a ayer, la racha actual pasa a considerarse cero hasta que el
     * usuario realice una nueva actividad.
     *
     * @param user usuario consultado.
     * @return racha del usuario.
     */
    @Transactional
    public UserStreak findOrCreate(User user) {
        UserStreak streak = repository.findById(user.getId())
                .orElseGet(() -> createInitial(user));

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate lastDate = streak.getLastActivityDate();
        if (lastDate != null && ChronoUnit.DAYS.between(lastDate, today) > 1
                && streak.getCurrentStreak() > 0) {
            // La racha se rompió por inactividad; la reflejamos sin
            // borrar el récord histórico ni la fecha del último acceso.
            streak.setCurrentStreak(0);
            streak.setStreakStartedOn(null);
            repository.save(streak);
        }

        return streak;
    }

    private UserStreak createInitial(User user) {
        UserStreak streak = new UserStreak();
        streak.setUser(user);
        streak.setCurrentStreak(0);
        streak.setLongestStreak(0);
        return repository.save(streak);
    }
}
