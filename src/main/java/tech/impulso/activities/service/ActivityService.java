package tech.impulso.activities.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.activities.dto.ActivityResponse;
import tech.impulso.activities.dto.AttemptResultResponse;
import tech.impulso.activities.dto.CreateActivityRequest;
import tech.impulso.activities.dto.SubmitAttemptRequest;
import tech.impulso.activities.dto.UpdateActivityRequest;
import tech.impulso.activities.entity.Activity;
import tech.impulso.activities.entity.ActivityAttempt;
import tech.impulso.activities.repository.ActivityAttemptRepository;
import tech.impulso.activities.repository.ActivityRepository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.gamification.service.BadgeService;
import tech.impulso.gamification.service.XpService;
import tech.impulso.lessons.entity.Lesson;
import tech.impulso.lessons.repository.LessonRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.util.List;

/**
 * Servicio con las operaciones sobre actividades y sus intentos
 * (RF-042).
 *
 * <p>Cubre la gestión completa por parte del instructor responsable del
 * curso al que pertenece la lección y la consulta y resolución por parte
 * del estudiante. Aplica la restricción de máximo de intentos, oculta
 * las respuestas correctas al estudiante y otorga XP en el primer
 * acierto.</p>
 */
@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final ActivityAttemptRepository attemptRepository;
    private final LessonRepository lessonRepository;
    private final ActivityGrader grader;
    private final CurrentUserService currentUserService;
    private final XpService xpService;
    private final BadgeService badgeService;

    public ActivityService(ActivityRepository activityRepository,
                           ActivityAttemptRepository attemptRepository,
                           LessonRepository lessonRepository,
                           ActivityGrader grader,
                           CurrentUserService currentUserService,
                           XpService xpService,
                           BadgeService badgeService) {
        this.activityRepository = activityRepository;
        this.attemptRepository = attemptRepository;
        this.lessonRepository = lessonRepository;
        this.grader = grader;
        this.currentUserService = currentUserService;
        this.xpService = xpService;
        this.badgeService = badgeService;
    }

    /**
     * Devuelve las actividades de la lección ordenadas por posición. Los
     * estudiantes sólo verán las publicadas.
     */
    @Transactional(readOnly = true)
    public List<ActivityResponse> listByLesson(Long lessonId) {
        Lesson lesson = loadLesson(lessonId);
        User current = currentUserService.requireAuthenticatedUser();
        boolean manages = canManage(lesson, current);
        ContentStatus filter = manages ? null : ContentStatus.PUBLICADO;
        if (!manages && lesson.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "La lección indicada no existe.");
        }
        return activityRepository.findByLesson(lessonId, filter).stream()
                .map(activity -> toResponse(activity, manages))
                .toList();
    }

    /**
     * Consulta el detalle de una actividad aplicando las reglas de
     * visibilidad correspondientes.
     */
    @Transactional(readOnly = true)
    public ActivityResponse findById(Long activityId) {
        Activity activity = loadActivity(activityId);
        User current = currentUserService.requireAuthenticatedUser();
        boolean manages = canManage(activity.getLesson(), current);
        if (!manages && activity.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "La actividad indicada no existe.");
        }
        return toResponse(activity, manages);
    }

    /**
     * Crea una nueva actividad dentro de la lección indicada.
     */
    @Transactional
    public ActivityResponse create(Long lessonId, CreateActivityRequest request) {
        Lesson lesson = loadLesson(lessonId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(lesson, current);

        grader.validateConfig(request.type(), request.config());

        int orderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : nextOrderIndex(lessonId);
        ensureOrderIndexAvailable(lessonId, orderIndex, null);

        Activity activity = new Activity();
        activity.setLesson(lesson);
        activity.setName(request.name().trim());
        activity.setDescription(request.description());
        activity.setInstructions(request.instructions());
        activity.setType(request.type());
        activity.setDifficulty(request.difficulty());
        activity.setMaxScore(request.maxScore() != null ? request.maxScore() : 100);
        activity.setXpReward(request.xpReward() != null ? request.xpReward() : 5);
        activity.setMaxAttempts(request.maxAttempts());
        activity.setOrderIndex(orderIndex);
        activity.setStatus(ContentStatus.BORRADOR);
        activity.setConfig(request.config());

        return toResponse(activityRepository.save(activity), true);
    }

    /**
     * Actualiza los campos permitidos de una actividad existente.
     */
    @Transactional
    public ActivityResponse update(Long activityId, UpdateActivityRequest request) {
        Activity activity = loadActivity(activityId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(activity.getLesson(), current);

        if (request.name() != null) {
            activity.setName(request.name().trim());
        }
        if (request.description() != null) {
            activity.setDescription(request.description());
        }
        if (request.instructions() != null) {
            activity.setInstructions(request.instructions());
        }
        if (request.difficulty() != null) {
            activity.setDifficulty(request.difficulty());
        }
        if (request.maxScore() != null) {
            activity.setMaxScore(request.maxScore());
        }
        if (request.xpReward() != null) {
            activity.setXpReward(request.xpReward());
        }
        if (request.maxAttempts() != null) {
            activity.setMaxAttempts(request.maxAttempts());
        }
        if (request.orderIndex() != null && !request.orderIndex().equals(activity.getOrderIndex())) {
            ensureOrderIndexAvailable(activity.getLesson().getId(), request.orderIndex(), activityId);
            activity.setOrderIndex(request.orderIndex());
        }
        if (request.config() != null) {
            grader.validateConfig(activity.getType(), request.config());
            activity.setConfig(request.config());
        }

        return toResponse(activity, true);
    }

    /**
     * Cambia el estado del ciclo de vida de la actividad.
     */
    @Transactional
    public ActivityResponse changeStatus(Long activityId, ContentStatus status) {
        Activity activity = loadActivity(activityId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(activity.getLesson(), current);
        activity.setStatus(status);
        return toResponse(activity, true);
    }

    /**
     * Elimina una actividad y sus intentos asociados.
     */
    @Transactional
    public void delete(Long activityId) {
        Activity activity = loadActivity(activityId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(activity.getLesson(), current);
        activityRepository.delete(activity);
    }

    /**
     * Registra un intento del estudiante sobre una actividad, aplicando
     * la evaluación automática y actualizando XP e insignias cuando
     * corresponda.
     */
    @Transactional
    public AttemptResultResponse submitAttempt(Long activityId, SubmitAttemptRequest request) {
        Activity activity = loadActivity(activityId);
        User user = currentUserService.requireAuthenticatedUser();

        if (activity.getStatus() != ContentStatus.PUBLICADO
                || activity.getLesson().getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.CONFLICT, "La actividad no está disponible para responder.");
        }

        long previousAttempts = attemptRepository.countByUserIdAndActivityId(user.getId(), activityId);
        if (activity.getMaxAttempts() != null && previousAttempts >= activity.getMaxAttempts()) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "Ha alcanzado el número máximo de intentos permitidos para esta actividad.");
        }

        boolean alreadyCorrect = attemptRepository.hasCorrectAttempt(user.getId(), activityId);
        boolean correct = grader.isCorrect(activity, request.answer());
        int score = correct ? activity.getMaxScore() : 0;

        ActivityAttempt attempt = new ActivityAttempt();
        attempt.setActivity(activity);
        attempt.setUser(user);
        attempt.setAnswer(request.answer());
        attempt.setCorrect(correct);
        attempt.setScore(score);
        attempt = attemptRepository.save(attempt);

        int xpAwarded = 0;
        if (correct && !alreadyCorrect && activity.getXpReward() > 0) {
            xpService.awardForActivityCompleted(user, activityId, activity.getXpReward());
            xpAwarded = activity.getXpReward();
            badgeService.evaluateAndAward(user);
        }

        long attemptsUsed = previousAttempts + 1;
        Integer attemptsLeft = activity.getMaxAttempts() == null
                ? null
                : Math.max(0, activity.getMaxAttempts() - (int) attemptsUsed);

        return AttemptResultResponse.from(attempt, activity.getMaxScore(), attemptsUsed, attemptsLeft, xpAwarded);
    }

    /**
     * Convierte una actividad a su representación pública, ocultando las
     * respuestas correctas cuando el consumidor es un estudiante.
     */
    private ActivityResponse toResponse(Activity activity, boolean forInstructor) {
        String config = forInstructor
                ? activity.getConfig()
                : grader.sanitizeConfigForStudent(activity.getType(), activity.getConfig());
        return ActivityResponse.from(activity, config);
    }

    private int nextOrderIndex(Long lessonId) {
        Integer current = activityRepository.findMaxOrderIndex(lessonId);
        return current == null ? 1 : current + 1;
    }

    private void ensureOrderIndexAvailable(Long lessonId, Integer orderIndex, Long ignoredActivityId) {
        activityRepository.findByLesson(lessonId, null).stream()
                .filter(existing -> existing.getOrderIndex().equals(orderIndex))
                .filter(existing -> ignoredActivityId == null || !existing.getId().equals(ignoredActivityId))
                .findAny()
                .ifPresent(conflict -> {
                    throw new BusinessException(HttpStatus.CONFLICT,
                            "Ya existe una actividad en la posición %d dentro de la lección.".formatted(orderIndex));
                });
    }

    private Lesson loadLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La lección indicada no existe."));
    }

    private Activity loadActivity(Long activityId) {
        return activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La actividad indicada no existe."));
    }

    private boolean canManage(Lesson lesson, User user) {
        if (user.getRole() == Role.ADMINISTRADOR) {
            return true;
        }
        return user.getRole() != Role.ESTUDIANTE
                && lesson.getModule().getCourse().getInstructor().getId().equals(user.getId());
    }

    private void ensureCanEdit(Lesson lesson, User user) {
        if (!canManage(lesson, user)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el instructor responsable o un administrador puede modificar las actividades de esta lección.");
        }
    }
}
