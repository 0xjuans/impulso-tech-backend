package tech.impulso.challenges.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.challenges.dto.ChallengeAttemptResponse;
import tech.impulso.challenges.dto.ChallengeResponse;
import tech.impulso.challenges.dto.CreateChallengeRequest;
import tech.impulso.challenges.dto.ReviewChallengeAttemptRequest;
import tech.impulso.challenges.dto.SubmitChallengeAttemptRequest;
import tech.impulso.challenges.dto.UpdateChallengeRequest;
import tech.impulso.challenges.entity.Challenge;
import tech.impulso.challenges.entity.ChallengeAttempt;
import tech.impulso.challenges.entity.ChallengeAttemptStatus;
import tech.impulso.challenges.repository.ChallengeAttemptRepository;
import tech.impulso.challenges.repository.ChallengeRepository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.entity.CourseModule;
import tech.impulso.courses.repository.CourseModuleRepository;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.gamification.service.BadgeService;
import tech.impulso.gamification.service.XpService;
import tech.impulso.learningroutes.repository.LearningRouteRepository;
import tech.impulso.lessons.repository.LessonRepository;
import tech.impulso.notifications.entity.NotificationType;
import tech.impulso.notifications.service.NotificationService;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Servicio con las operaciones sobre retos de programación y sus
 * intentos (RF-014).
 *
 * <p>Mientras no exista el servicio de ejecución automática de código
 * (RF-037), el estudiante envía su solución y el instructor revisa
 * manualmente el intento marcándolo como aprobado o rechazado. La
 * primera aprobación otorga XP configurable, dispara notificación y
 * reevalúa insignias.</p>
 */
@Service
public class ChallengeService {

    /** Sentinel para desasociar vínculos opcionales en updates. */
    private static final long UNLINK_SENTINEL = -1L;

    private final ChallengeRepository challengeRepository;
    private final ChallengeAttemptRepository attemptRepository;
    private final LearningRouteRepository learningRouteRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CurrentUserService currentUserService;
    private final XpService xpService;
    private final BadgeService badgeService;
    private final NotificationService notificationService;

    public ChallengeService(ChallengeRepository challengeRepository,
                            ChallengeAttemptRepository attemptRepository,
                            LearningRouteRepository learningRouteRepository,
                            CourseRepository courseRepository,
                            CourseModuleRepository moduleRepository,
                            LessonRepository lessonRepository,
                            CurrentUserService currentUserService,
                            XpService xpService,
                            BadgeService badgeService,
                            NotificationService notificationService) {
        this.challengeRepository = challengeRepository;
        this.attemptRepository = attemptRepository;
        this.learningRouteRepository = learningRouteRepository;
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.currentUserService = currentUserService;
        this.xpService = xpService;
        this.badgeService = badgeService;
        this.notificationService = notificationService;
    }

    // ------------------- Catálogo de retos -------------------

    @Transactional(readOnly = true)
    public PagedResponse<ChallengeResponse> listPublished(String search,
                                                          DifficultyLevel difficulty,
                                                          String language,
                                                          Long learningRouteId,
                                                          Long courseId,
                                                          Long moduleId,
                                                          Long lessonId,
                                                          Pageable pageable) {
        Page<Challenge> page = challengeRepository.search(normalize(search), difficulty,
                ContentStatus.PUBLICADO, normalize(language),
                learningRouteId, courseId, moduleId, lessonId, pageable);
        return PagedResponse.from(page, challenge -> ChallengeResponse.from(challenge, false));
    }

    @Transactional(readOnly = true)
    public PagedResponse<ChallengeResponse> listAll(String search,
                                                    DifficultyLevel difficulty,
                                                    ContentStatus status,
                                                    String language,
                                                    Long learningRouteId,
                                                    Long courseId,
                                                    Long moduleId,
                                                    Long lessonId,
                                                    Pageable pageable) {
        Page<Challenge> page = challengeRepository.search(normalize(search), difficulty, status,
                normalize(language), learningRouteId, courseId, moduleId, lessonId, pageable);
        return PagedResponse.from(page, challenge -> ChallengeResponse.from(challenge, true));
    }

    @Transactional(readOnly = true)
    public ChallengeResponse findById(Long id) {
        Challenge challenge = loadChallenge(id);
        User current = currentUserService.requireAuthenticatedUser();
        boolean manages = canManage(challenge, current);
        if (!manages && challenge.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El reto indicado no existe.");
        }
        return ChallengeResponse.from(challenge, manages);
    }

    @Transactional
    public ChallengeResponse create(CreateChallengeRequest request) {
        User author = currentUserService.requireAuthenticatedUser();
        ensureCanManageRole(author);

        Challenge challenge = new Challenge();
        challenge.setName(request.name().trim());
        challenge.setDescription(request.description());
        challenge.setObjective(request.objective());
        challenge.setInstructions(request.instructions());
        challenge.setDifficulty(request.difficulty());
        challenge.setAllowedLanguages(request.allowedLanguages().trim());
        challenge.setIoExamples(request.ioExamples());
        challenge.setRestrictions(request.restrictions());
        challenge.setPublicTestCases(request.publicTestCases());
        challenge.setHiddenTestCases(request.hiddenTestCases());
        challenge.setXpReward(request.xpReward() != null ? request.xpReward() : 50);
        challenge.setEstimatedMinutes(request.estimatedMinutes());
        challenge.setStatus(ContentStatus.BORRADOR);
        challenge.setInstructor(author);
        assignLinks(challenge, request.learningRouteId(), request.courseId(),
                request.moduleId(), request.lessonId(), false);

        return ChallengeResponse.from(challengeRepository.save(challenge), true);
    }

    @Transactional
    public ChallengeResponse update(Long id, UpdateChallengeRequest request) {
        Challenge challenge = loadChallenge(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(challenge, current);

        if (request.name() != null) {
            challenge.setName(request.name().trim());
        }
        if (request.description() != null) {
            challenge.setDescription(request.description());
        }
        if (request.objective() != null) {
            challenge.setObjective(request.objective());
        }
        if (request.instructions() != null) {
            challenge.setInstructions(request.instructions());
        }
        if (request.difficulty() != null) {
            challenge.setDifficulty(request.difficulty());
        }
        if (request.allowedLanguages() != null) {
            challenge.setAllowedLanguages(request.allowedLanguages().trim());
        }
        if (request.ioExamples() != null) {
            challenge.setIoExamples(request.ioExamples());
        }
        if (request.restrictions() != null) {
            challenge.setRestrictions(request.restrictions());
        }
        if (request.publicTestCases() != null) {
            challenge.setPublicTestCases(request.publicTestCases());
        }
        if (request.hiddenTestCases() != null) {
            challenge.setHiddenTestCases(request.hiddenTestCases());
        }
        if (request.xpReward() != null) {
            challenge.setXpReward(request.xpReward());
        }
        if (request.estimatedMinutes() != null) {
            challenge.setEstimatedMinutes(request.estimatedMinutes());
        }
        assignLinks(challenge, request.learningRouteId(), request.courseId(),
                request.moduleId(), request.lessonId(), true);

        return ChallengeResponse.from(challenge, true);
    }

    @Transactional
    public ChallengeResponse changeStatus(Long id, ContentStatus status) {
        Challenge challenge = loadChallenge(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(challenge, current);
        challenge.setStatus(status);
        return ChallengeResponse.from(challenge, true);
    }

    @Transactional
    public void delete(Long id) {
        Challenge challenge = loadChallenge(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(challenge, current);
        challengeRepository.delete(challenge);
    }

    // ------------------- Intentos -------------------

    /**
     * Registra un nuevo intento del estudiante autenticado sobre el
     * reto indicado. Valida que el lenguaje esté entre los permitidos
     * y asigna automáticamente el número de intento.
     */
    @Transactional
    public ChallengeAttemptResponse submit(Long challengeId, SubmitChallengeAttemptRequest request) {
        Challenge challenge = loadChallenge(challengeId);
        User user = currentUserService.requireAuthenticatedUser();

        if (challenge.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "El reto no está disponible para nuevos intentos.");
        }

        String language = request.language().trim();
        if (!isLanguageAllowed(challenge.getAllowedLanguages(), language)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El lenguaje '%s' no está permitido para este reto.".formatted(language));
        }

        Integer previous = attemptRepository.findMaxAttemptNumber(user.getId(), challengeId);
        int next = previous == null ? 1 : previous + 1;

        ChallengeAttempt attempt = new ChallengeAttempt();
        attempt.setChallenge(challenge);
        attempt.setUser(user);
        attempt.setAttemptNumber(next);
        attempt.setLanguage(language);
        attempt.setCode(request.code());
        attempt.setStatus(ChallengeAttemptStatus.PENDIENTE);
        return ChallengeAttemptResponse.from(attemptRepository.save(attempt));
    }

    /**
     * Devuelve los intentos del usuario autenticado sobre un reto,
     * ordenados del más reciente al más antiguo.
     */
    @Transactional(readOnly = true)
    public List<ChallengeAttemptResponse> listMyAttempts(Long challengeId) {
        User user = currentUserService.requireAuthenticatedUser();
        return attemptRepository.findByUserIdAndChallengeIdOrderByAttemptNumberDesc(user.getId(), challengeId).stream()
                .map(ChallengeAttemptResponse::from)
                .toList();
    }

    /**
     * Devuelve los intentos recibidos en un reto. Reservado al
     * instructor responsable o a un administrador.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ChallengeAttemptResponse> listChallengeAttempts(Long challengeId,
                                                                         ChallengeAttemptStatus status,
                                                                         Pageable pageable) {
        Challenge challenge = loadChallenge(challengeId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(challenge, current);
        Page<ChallengeAttempt> page = attemptRepository.findByChallenge(challengeId, status, pageable);
        return PagedResponse.from(page, ChallengeAttemptResponse::from);
    }

    /**
     * Revisa un intento y aplica el nuevo estado. La primera aprobación
     * otorga XP configurable y dispara notificación al estudiante.
     */
    @Transactional
    public ChallengeAttemptResponse review(Long attemptId, ReviewChallengeAttemptRequest request) {
        if (request.status() == ChallengeAttemptStatus.PENDIENTE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El estado indicado no es válido para una revisión.");
        }

        ChallengeAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El intento indicado no existe."));
        Challenge challenge = attempt.getChallenge();
        User reviewer = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(challenge, reviewer);

        boolean alreadyApproved = attemptRepository.hasApprovedAttempt(
                attempt.getUser().getId(), challenge.getId());

        attempt.setStatus(request.status());
        attempt.setFeedback(request.feedback());
        attempt.setReviewedBy(reviewer);
        attempt.setReviewedAt(OffsetDateTime.now(ZoneOffset.UTC));

        User student = attempt.getUser();
        if (request.status() == ChallengeAttemptStatus.APROBADO) {
            if (!alreadyApproved && challenge.getXpReward() > 0) {
                xpService.awardForChallengeSolved(student, challenge.getId(), challenge.getXpReward());
                badgeService.evaluateAndAward(student);
            }
            notificationService.notify(
                    student,
                    NotificationType.GENERIC,
                    "Reto aprobado: %s".formatted(challenge.getName()),
                    "Tu intento #%d fue aprobado por el instructor.".formatted(attempt.getAttemptNumber()),
                    "CHALLENGE",
                    challenge.getId()
            );
        } else if (request.status() == ChallengeAttemptStatus.RECHAZADO) {
            notificationService.notify(
                    student,
                    NotificationType.GENERIC,
                    "Intento no aprobado en %s".formatted(challenge.getName()),
                    "El instructor no aprobó tu intento #%d. Revisa la retroalimentación y vuelve a intentarlo."
                            .formatted(attempt.getAttemptNumber()),
                    "CHALLENGE",
                    challenge.getId()
            );
        }

        return ChallengeAttemptResponse.from(attempt);
    }

    // ------------------- Utilidades internas -------------------

    /**
     * Determina si el lenguaje solicitado por el estudiante está entre
     * los permitidos por el reto (comparación insensible a mayúsculas y
     * espacios).
     */
    private boolean isLanguageAllowed(String allowed, String language) {
        if (allowed == null || language == null) {
            return false;
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(allowed.split(","))
                .map(String::trim)
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.equals(normalized));
    }

    private void assignLinks(Challenge challenge, Long learningRouteId, Long courseId,
                             Long moduleId, Long lessonId, boolean allowUnlink) {
        if (learningRouteId != null) {
            challenge.setLearningRoute(resolveOptional(learningRouteId, allowUnlink,
                    id -> learningRouteRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "La ruta de aprendizaje indicada no existe."))));
        }
        if (courseId != null) {
            challenge.setCourse(resolveOptional(courseId, allowUnlink,
                    id -> courseRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "El curso indicado no existe."))));
        }
        if (moduleId != null) {
            challenge.setModule(resolveOptional(moduleId, allowUnlink,
                    id -> moduleRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "El módulo indicado no existe."))));
        }
        if (lessonId != null) {
            challenge.setLesson(resolveOptional(lessonId, allowUnlink,
                    id -> lessonRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "La lección indicada no existe."))));
        }
    }

    private <T> T resolveOptional(Long id, boolean allowUnlink, Function<Long, T> loader) {
        if (allowUnlink && id == UNLINK_SENTINEL) {
            return null;
        }
        return loader.apply(id);
    }

    private Challenge loadChallenge(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El reto indicado no existe."));
    }

    private boolean canManage(Challenge challenge, User user) {
        if (user.getRole() == Role.ADMINISTRADOR) {
            return true;
        }
        return user.getRole() != Role.ESTUDIANTE
                && challenge.getInstructor().getId().equals(user.getId());
    }

    private void ensureCanManageRole(User user) {
        if (user.getRole() == Role.ESTUDIANTE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No cuenta con permisos para gestionar retos.");
        }
    }

    private void ensureCanEdit(Challenge challenge, User user) {
        if (!canManage(challenge, user)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el instructor responsable o un administrador puede modificar este reto.");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /** Suprime warning por el uso del tipo dentro del switch de módulos. */
    @SuppressWarnings("unused")
    private CourseModule unused(CourseModule module) {
        return module;
    }
}
