package tech.impulso.evaluations.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.activities.entity.Activity;
import tech.impulso.activities.entity.ActivityType;
import tech.impulso.activities.service.ActivityGrader;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.evaluations.dto.CreateEvaluationQuestionRequest;
import tech.impulso.evaluations.dto.CreateEvaluationRequest;
import tech.impulso.evaluations.dto.EvaluationAttemptResponse;
import tech.impulso.evaluations.dto.EvaluationQuestionResponse;
import tech.impulso.evaluations.dto.EvaluationResponse;
import tech.impulso.evaluations.dto.SubmitEvaluationRequest;
import tech.impulso.evaluations.dto.UpdateEvaluationRequest;
import tech.impulso.evaluations.entity.Evaluation;
import tech.impulso.evaluations.entity.EvaluationAttempt;
import tech.impulso.evaluations.entity.EvaluationQuestion;
import tech.impulso.evaluations.repository.EvaluationAttemptRepository;
import tech.impulso.evaluations.repository.EvaluationQuestionRepository;
import tech.impulso.evaluations.repository.EvaluationRepository;
import tech.impulso.lessons.entity.Lesson;
import tech.impulso.lessons.repository.LessonRepository;
import tech.impulso.notifications.entity.NotificationType;
import tech.impulso.notifications.service.NotificationService;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Servicio con las operaciones sobre evaluaciones y sus intentos
 * (RF-015 / RF-043).
 *
 * <p>Cubre la creación y edición por parte del instructor, la
 * realización del intento por parte del estudiante (con control de
 * tiempo y de número máximo de intentos) y la calificación automática
 * al enviar el intento.</p>
 */
@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final EvaluationQuestionRepository questionRepository;
    private final EvaluationAttemptRepository attemptRepository;
    private final LessonRepository lessonRepository;
    private final ActivityGrader grader;
    private final ObjectMapper objectMapper;
    private final CurrentUserService currentUserService;
    private final NotificationService notificationService;

    public EvaluationService(EvaluationRepository evaluationRepository,
                             EvaluationQuestionRepository questionRepository,
                             EvaluationAttemptRepository attemptRepository,
                             LessonRepository lessonRepository,
                             ActivityGrader grader,
                             ObjectMapper objectMapper,
                             CurrentUserService currentUserService,
                             NotificationService notificationService) {
        this.evaluationRepository = evaluationRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.lessonRepository = lessonRepository;
        this.grader = grader;
        this.objectMapper = objectMapper;
        this.currentUserService = currentUserService;
        this.notificationService = notificationService;
    }

    /**
     * Lista las evaluaciones de una lección ordenadas por posición.
     */
    @Transactional(readOnly = true)
    public List<EvaluationResponse> listByLesson(Long lessonId) {
        Lesson lesson = loadLesson(lessonId);
        User current = currentUserService.requireAuthenticatedUser();
        boolean manages = canManage(lesson, current);
        if (!manages && lesson.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "La lección indicada no existe.");
        }
        ContentStatus filter = manages ? null : ContentStatus.PUBLICADO;
        return evaluationRepository.findByLesson(lessonId, filter).stream()
                .map(evaluation -> toResponse(evaluation, manages))
                .toList();
    }

    /**
     * Consulta el detalle de una evaluación aplicando las reglas de
     * visibilidad correspondientes.
     */
    @Transactional(readOnly = true)
    public EvaluationResponse findById(Long evaluationId) {
        Evaluation evaluation = loadEvaluation(evaluationId);
        User current = currentUserService.requireAuthenticatedUser();
        boolean manages = canManage(evaluation.getLesson(), current);
        if (!manages && evaluation.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "La evaluación indicada no existe.");
        }
        return toResponse(evaluation, manages);
    }

    /**
     * Crea una nueva evaluación dentro de la lección indicada.
     */
    @Transactional
    public EvaluationResponse create(Long lessonId, CreateEvaluationRequest request) {
        Lesson lesson = loadLesson(lessonId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(lesson, current);

        int orderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : nextEvaluationOrder(lessonId);
        ensureEvaluationOrderAvailable(lessonId, orderIndex, null);

        Evaluation evaluation = new Evaluation();
        evaluation.setLesson(lesson);
        evaluation.setName(request.name().trim());
        evaluation.setDescription(request.description());
        evaluation.setInstructions(request.instructions());
        evaluation.setTimeLimitMinutes(request.timeLimitMinutes());
        evaluation.setPassingPercentage(request.passingPercentage() != null ? request.passingPercentage() : 60);
        evaluation.setMaxAttempts(request.maxAttempts() != null ? request.maxAttempts() : 1);
        evaluation.setOrderIndex(orderIndex);
        evaluation.setStatus(ContentStatus.BORRADOR);

        return toResponse(evaluationRepository.save(evaluation), true);
    }

    /**
     * Actualiza los campos permitidos de una evaluación existente.
     */
    @Transactional
    public EvaluationResponse update(Long evaluationId, UpdateEvaluationRequest request) {
        Evaluation evaluation = loadEvaluation(evaluationId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(evaluation.getLesson(), current);

        if (request.name() != null) {
            evaluation.setName(request.name().trim());
        }
        if (request.description() != null) {
            evaluation.setDescription(request.description());
        }
        if (request.instructions() != null) {
            evaluation.setInstructions(request.instructions());
        }
        if (request.timeLimitMinutes() != null) {
            evaluation.setTimeLimitMinutes(request.timeLimitMinutes());
        }
        if (request.passingPercentage() != null) {
            evaluation.setPassingPercentage(request.passingPercentage());
        }
        if (request.maxAttempts() != null) {
            evaluation.setMaxAttempts(request.maxAttempts());
        }
        if (request.orderIndex() != null && !request.orderIndex().equals(evaluation.getOrderIndex())) {
            ensureEvaluationOrderAvailable(evaluation.getLesson().getId(), request.orderIndex(), evaluationId);
            evaluation.setOrderIndex(request.orderIndex());
        }

        return toResponse(evaluation, true);
    }

    /**
     * Cambia el estado del ciclo de vida de una evaluación.
     */
    @Transactional
    public EvaluationResponse changeStatus(Long evaluationId, ContentStatus status) {
        Evaluation evaluation = loadEvaluation(evaluationId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(evaluation.getLesson(), current);
        evaluation.setStatus(status);
        return toResponse(evaluation, true);
    }

    /**
     * Elimina una evaluación y sus preguntas e intentos asociados.
     */
    @Transactional
    public void delete(Long evaluationId) {
        Evaluation evaluation = loadEvaluation(evaluationId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(evaluation.getLesson(), current);
        evaluationRepository.delete(evaluation);
    }

    /**
     * Agrega una pregunta a la evaluación indicada.
     */
    @Transactional
    public EvaluationResponse addQuestion(Long evaluationId, CreateEvaluationQuestionRequest request) {
        Evaluation evaluation = loadEvaluation(evaluationId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(evaluation.getLesson(), current);

        grader.validateConfig(request.type(), request.config());

        int orderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : nextQuestionOrder(evaluationId);
        ensureQuestionOrderAvailable(evaluationId, orderIndex, null);

        EvaluationQuestion question = new EvaluationQuestion();
        question.setEvaluation(evaluation);
        question.setOrderIndex(orderIndex);
        question.setType(request.type());
        question.setQuestionText(request.questionText());
        question.setScore(request.score());
        question.setConfig(request.config());
        questionRepository.save(question);

        return toResponse(evaluation, true);
    }

    /**
     * Elimina una pregunta específica de una evaluación.
     */
    @Transactional
    public void deleteQuestion(Long questionId) {
        EvaluationQuestion question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La pregunta indicada no existe."));
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(question.getEvaluation().getLesson(), current);
        questionRepository.delete(question);
    }

    /**
     * Inicia un intento del estudiante sobre la evaluación.
     */
    @Transactional
    public EvaluationAttemptResponse startAttempt(Long evaluationId) {
        Evaluation evaluation = loadEvaluation(evaluationId);
        User user = currentUserService.requireAuthenticatedUser();

        if (evaluation.getStatus() != ContentStatus.PUBLICADO
                || evaluation.getLesson().getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.CONFLICT, "La evaluación no está disponible.");
        }

        return attemptRepository.findInProgress(user.getId(), evaluationId)
                .map(EvaluationAttemptResponse::from)
                .orElseGet(() -> {
                    long finished = attemptRepository.countFinished(user.getId(), evaluationId);
                    if (finished >= evaluation.getMaxAttempts()) {
                        throw new BusinessException(HttpStatus.CONFLICT,
                                "Ha alcanzado el número máximo de intentos permitidos para esta evaluación.");
                    }

                    List<EvaluationQuestion> questions =
                            questionRepository.findByEvaluationIdOrderByOrderIndexAsc(evaluationId);
                    if (questions.isEmpty()) {
                        throw new BusinessException(HttpStatus.CONFLICT,
                                "La evaluación aún no cuenta con preguntas configuradas.");
                    }

                    int maxScore = questions.stream().mapToInt(EvaluationQuestion::getScore).sum();
                    EvaluationAttempt attempt = new EvaluationAttempt();
                    attempt.setEvaluation(evaluation);
                    attempt.setUser(user);
                    attempt.setMaxPossibleScore(maxScore);
                    return EvaluationAttemptResponse.from(attemptRepository.save(attempt));
                });
    }

    /**
     * Envía las respuestas del estudiante y calcula el resultado del
     * intento en curso.
     */
    @Transactional
    public EvaluationAttemptResponse submitAttempt(Long evaluationId, SubmitEvaluationRequest request) {
        Evaluation evaluation = loadEvaluation(evaluationId);
        User user = currentUserService.requireAuthenticatedUser();

        EvaluationAttempt attempt = attemptRepository.findInProgress(user.getId(), evaluationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT,
                        "Debe iniciar un intento antes de enviar respuestas."));

        List<EvaluationQuestion> questions =
                questionRepository.findByEvaluationIdOrderByOrderIndexAsc(evaluationId);
        JsonNode answersNode = parseAnswers(request.answers());

        int totalScore = 0;
        int maxScore = 0;
        for (EvaluationQuestion question : questions) {
            maxScore += question.getScore();
            JsonNode answer = answersNode.get(String.valueOf(question.getId()));
            if (answer == null || answer.isNull()) {
                continue;
            }
            if (gradeQuestion(question, answer)) {
                totalScore += question.getScore();
            }
        }

        int percentage = maxScore == 0
                ? 0
                : (int) Math.round((totalScore * 100.0) / maxScore);
        boolean passed = percentage >= evaluation.getPassingPercentage();

        attempt.setAnswers(request.answers());
        attempt.setTotalScore(totalScore);
        attempt.setMaxPossibleScore(maxScore);
        attempt.setPercentage(percentage);
        attempt.setPassed(passed);
        attempt.setFinishedAt(OffsetDateTime.now(ZoneOffset.UTC));

        // Marcamos el intento como finalizado incluso si el estudiante
        // se pasó del tiempo, pero registramos que no aprobó cuando
        // sobrepasó el límite establecido.
        if (evaluation.getTimeLimitMinutes() != null) {
            long minutesUsed = ChronoUnit.MINUTES.between(attempt.getStartedAt(), attempt.getFinishedAt());
            if (minutesUsed > evaluation.getTimeLimitMinutes()) {
                attempt.setPassed(false);
            }
        }

        EvaluationAttempt saved = attemptRepository.save(attempt);

        // Notificamos al estudiante el resultado obtenido.
        if (saved.isPassed()) {
            notificationService.notify(
                    user,
                    NotificationType.EVALUATION_PASSED,
                    "Aprobaste: %s".formatted(evaluation.getName()),
                    "Obtuviste el %d%% en la evaluación \"%s\". ¡Excelente trabajo!"
                            .formatted(saved.getPercentage(), evaluation.getName()),
                    "EVALUATION",
                    evaluation.getId()
            );
        } else {
            notificationService.notify(
                    user,
                    NotificationType.EVALUATION_FAILED,
                    "Resultado: %s".formatted(evaluation.getName()),
                    "Obtuviste el %d%% en la evaluación \"%s\". Puedes revisar el contenido e intentarlo nuevamente."
                            .formatted(saved.getPercentage(), evaluation.getName()),
                    "EVALUATION",
                    evaluation.getId()
            );
        }
        return EvaluationAttemptResponse.from(saved);
    }

    /**
     * Devuelve el historial de intentos finalizados del usuario en la
     * evaluación indicada.
     */
    @Transactional(readOnly = true)
    public List<EvaluationAttemptResponse> listMyAttempts(Long evaluationId) {
        User user = currentUserService.requireAuthenticatedUser();
        return attemptRepository.findFinishedByUser(user.getId(), evaluationId).stream()
                .map(EvaluationAttemptResponse::from)
                .toList();
    }

    /**
     * Convierte la evaluación y sus preguntas a la representación
     * pública, ocultando las respuestas correctas cuando el consumidor
     * es un estudiante.
     */
    private EvaluationResponse toResponse(Evaluation evaluation, boolean forInstructor) {
        List<EvaluationQuestionResponse> questions =
                questionRepository.findByEvaluationIdOrderByOrderIndexAsc(evaluation.getId()).stream()
                        .map(question -> {
                            String config = forInstructor
                                    ? question.getConfig()
                                    : grader.sanitizeConfigForStudent(question.getType(), question.getConfig());
                            return EvaluationQuestionResponse.from(question, config);
                        })
                        .toList();
        return EvaluationResponse.from(evaluation, questions);
    }

    /**
     * Evalúa una respuesta individual reutilizando la lógica del
     * {@link ActivityGrader} a través de una actividad temporal.
     */
    private boolean gradeQuestion(EvaluationQuestion question, JsonNode answer) {
        Activity adapter = new Activity();
        adapter.setType(question.getType());
        adapter.setConfig(question.getConfig());
        try {
            String answerJson = objectMapper.writeValueAsString(answer);
            return grader.isCorrect(adapter, answerJson);
        } catch (Exception ex) {
            return false;
        }
    }

    private JsonNode parseAnswers(String answers) {
        try {
            return objectMapper.readTree(answers);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El cuerpo de respuestas no es un JSON válido.");
        }
    }

    private int nextEvaluationOrder(Long lessonId) {
        Integer current = evaluationRepository.findMaxOrderIndex(lessonId);
        return current == null ? 1 : current + 1;
    }

    private void ensureEvaluationOrderAvailable(Long lessonId, Integer orderIndex, Long ignoredEvaluationId) {
        evaluationRepository.findByLesson(lessonId, null).stream()
                .filter(existing -> existing.getOrderIndex().equals(orderIndex))
                .filter(existing -> ignoredEvaluationId == null || !existing.getId().equals(ignoredEvaluationId))
                .findAny()
                .ifPresent(conflict -> {
                    throw new BusinessException(HttpStatus.CONFLICT,
                            "Ya existe una evaluación en la posición %d dentro de la lección.".formatted(orderIndex));
                });
    }

    private int nextQuestionOrder(Long evaluationId) {
        return questionRepository.findByEvaluationIdOrderByOrderIndexAsc(evaluationId).stream()
                .mapToInt(EvaluationQuestion::getOrderIndex)
                .max()
                .orElse(0) + 1;
    }

    private void ensureQuestionOrderAvailable(Long evaluationId, Integer orderIndex, Long ignoredQuestionId) {
        questionRepository.findByEvaluationIdOrderByOrderIndexAsc(evaluationId).stream()
                .filter(existing -> existing.getOrderIndex().equals(orderIndex))
                .filter(existing -> ignoredQuestionId == null || !existing.getId().equals(ignoredQuestionId))
                .findAny()
                .ifPresent(conflict -> {
                    throw new BusinessException(HttpStatus.CONFLICT,
                            "Ya existe una pregunta en la posición %d dentro de la evaluación.".formatted(orderIndex));
                });
    }

    private Lesson loadLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La lección indicada no existe."));
    }

    private Evaluation loadEvaluation(Long evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La evaluación indicada no existe."));
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
                    "Solo el instructor responsable o un administrador puede modificar esta evaluación.");
        }
    }

    /** Silencia el aviso del compilador sobre el uso del tipo. */
    @SuppressWarnings("unused")
    private ActivityType typeReference(ActivityType type) {
        return type;
    }
}
