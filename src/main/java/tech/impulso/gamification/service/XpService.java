package tech.impulso.gamification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.gamification.entity.UserXp;
import tech.impulso.gamification.entity.XpEvent;
import tech.impulso.gamification.entity.XpSource;
import tech.impulso.gamification.repository.UserXpRepository;
import tech.impulso.gamification.repository.XpEventRepository;
import tech.impulso.notifications.entity.NotificationType;
import tech.impulso.notifications.service.NotificationService;
import tech.impulso.users.entity.User;

/**
 * Servicio responsable de otorgar experiencia a los usuarios y
 * mantener el agregado con la XP total y el nivel actual (RF-018 /
 * RF-047).
 *
 * <p>Los otorgamientos son idempotentes: cada evento se identifica por
 * la combinación de fuente y recurso, y sólo genera XP la primera vez
 * que se registra. Los valores otorgados por defecto se pueden
 * configurar mediante variables del namespace {@code app.gamification.xp}.</p>
 */
@Service
public class XpService {

    private final UserXpRepository userXpRepository;
    private final XpEventRepository xpEventRepository;
    private final NotificationService notificationService;

    private final int lessonCompletedXp;
    private final int courseCompletedXp;
    private final int xpPerLevel;

    public XpService(UserXpRepository userXpRepository,
                     XpEventRepository xpEventRepository,
                     NotificationService notificationService,
                     @Value("${app.gamification.xp.lesson-completed:10}") int lessonCompletedXp,
                     @Value("${app.gamification.xp.course-completed:200}") int courseCompletedXp,
                     @Value("${app.gamification.xp.per-level:100}") int xpPerLevel) {
        this.userXpRepository = userXpRepository;
        this.xpEventRepository = xpEventRepository;
        this.notificationService = notificationService;
        this.lessonCompletedXp = lessonCompletedXp;
        this.courseCompletedXp = courseCompletedXp;
        this.xpPerLevel = Math.max(1, xpPerLevel);
    }

    /**
     * Otorga la experiencia correspondiente por completar una lección.
     *
     * @param user     estudiante que recibe la experiencia.
     * @param lessonId identificador de la lección completada.
     */
    @Transactional
    public void awardForLessonCompleted(User user, Long lessonId) {
        awardIfNew(user, XpSource.LESSON_COMPLETED, lessonId, lessonCompletedXp);
    }

    /**
     * Otorga la experiencia correspondiente por completar un curso.
     *
     * @param user     estudiante que recibe la experiencia.
     * @param courseId identificador del curso completado.
     */
    @Transactional
    public void awardForCourseCompleted(User user, Long courseId) {
        awardIfNew(user, XpSource.COURSE_COMPLETED, courseId, courseCompletedXp);
    }

    /**
     * Otorga la experiencia definida por el instructor al responder por
     * primera vez correctamente una actividad. La cantidad de XP la
     * suministra el llamador porque cada actividad configura su propia
     * recompensa.
     *
     * @param user       estudiante beneficiado.
     * @param activityId identificador de la actividad respondida.
     * @param xpAmount   XP a otorgar; valores no positivos se ignoran.
     */
    @Transactional
    public void awardForActivityCompleted(User user, Long activityId, int xpAmount) {
        awardIfNew(user, XpSource.ACTIVITY_COMPLETED, activityId, xpAmount);
    }

    /**
     * Devuelve el estado actual de XP y nivel del usuario. Si aún no
     * existe agregado se crea uno con valores iniciales.
     *
     * @param user usuario consultado.
     * @return agregado de XP y nivel.
     */
    @Transactional
    public UserXp findOrCreate(User user) {
        return userXpRepository.findById(user.getId())
                .orElseGet(() -> createInitial(user));
    }

    /**
     * Registra el otorgamiento sólo cuando no existe un evento previo
     * equivalente. Actualiza el agregado del usuario con la nueva XP
     * total y recalcula el nivel.
     *
     * @param user       estudiante beneficiado.
     * @param source     tipo de fuente que originó la recompensa.
     * @param sourceId   identificador del recurso asociado.
     * @param xpAmount   cantidad de XP a otorgar; se ignora si es cero
     *                   o negativa.
     */
    private void awardIfNew(User user, XpSource source, Long sourceId, int xpAmount) {
        if (xpAmount <= 0) {
            return;
        }
        if (xpEventRepository.existsByUserIdAndSourceTypeAndSourceId(user.getId(), source, sourceId)) {
            return;
        }

        XpEvent event = new XpEvent();
        event.setUser(user);
        event.setSourceType(source);
        event.setSourceId(sourceId);
        event.setXpAwarded(xpAmount);
        xpEventRepository.save(event);

        UserXp aggregate = userXpRepository.findById(user.getId())
                .orElseGet(() -> createInitial(user));
        int previousLevel = aggregate.getCurrentLevel();
        aggregate.setTotalXp(aggregate.getTotalXp() + xpAmount);
        int newLevel = computeLevel(aggregate.getTotalXp());
        aggregate.setCurrentLevel(newLevel);
        userXpRepository.save(aggregate);

        if (newLevel > previousLevel) {
            notificationService.notify(
                    user,
                    NotificationType.LEVEL_UP,
                    "¡Subiste de nivel!",
                    "Alcanzaste el nivel %d en Impulso Tech.".formatted(newLevel),
                    "USER",
                    user.getId()
            );
        }
    }

    /**
     * Crea el agregado inicial de un usuario con cero XP y nivel 1.
     *
     * @param user usuario propietario del agregado.
     * @return el agregado recién creado.
     */
    private UserXp createInitial(User user) {
        UserXp aggregate = new UserXp();
        aggregate.setUser(user);
        aggregate.setTotalXp(0);
        aggregate.setCurrentLevel(1);
        return userXpRepository.save(aggregate);
    }

    /**
     * Calcula el nivel correspondiente a una cantidad de XP acumulada
     * utilizando una progresión lineal: cada bloque de {@code xpPerLevel}
     * puntos otorga un nivel adicional, comenzando por el nivel 1.
     *
     * @param totalXp XP total del usuario.
     * @return nivel resultante (mínimo 1).
     */
    public int computeLevel(int totalXp) {
        return Math.max(1, (totalXp / xpPerLevel) + 1);
    }

    /**
     * Cantidad de XP necesaria para alcanzar el nivel indicado.
     *
     * @param level nivel objetivo.
     * @return XP total requerida para alcanzarlo.
     */
    public int xpRequiredForLevel(int level) {
        return Math.max(0, (level - 1) * xpPerLevel);
    }
}
