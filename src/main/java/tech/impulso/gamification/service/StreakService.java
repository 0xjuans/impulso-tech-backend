package tech.impulso.gamification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.gamification.entity.UserStreak;
import tech.impulso.gamification.repository.UserStreakRepository;
import tech.impulso.users.entity.User;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

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

    private final UserStreakRepository repository;

    public StreakService(UserStreakRepository repository) {
        this.repository = repository;
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
        if (streak.getCurrentStreak() > streak.getLongestStreak()) {
            streak.setLongestStreak(streak.getCurrentStreak());
        }
        return repository.save(streak);
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
