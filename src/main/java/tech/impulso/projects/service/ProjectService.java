package tech.impulso.projects.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.entity.Course;
import tech.impulso.courses.entity.CourseModule;
import tech.impulso.courses.repository.CourseModuleRepository;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.gamification.service.BadgeService;
import tech.impulso.gamification.service.XpService;
import tech.impulso.learningroutes.entity.LearningRoute;
import tech.impulso.learningroutes.repository.LearningRouteRepository;
import tech.impulso.notifications.entity.NotificationType;
import tech.impulso.notifications.service.NotificationService;
import tech.impulso.projects.dto.CreateProjectRequest;
import tech.impulso.projects.dto.CreateSubmissionRequest;
import tech.impulso.projects.dto.ProjectResponse;
import tech.impulso.projects.dto.ProjectSubmissionResponse;
import tech.impulso.projects.dto.ReviewSubmissionRequest;
import tech.impulso.projects.dto.UpdateProjectRequest;
import tech.impulso.projects.entity.Project;
import tech.impulso.projects.entity.ProjectSubmission;
import tech.impulso.projects.entity.ProjectSubmissionStatus;
import tech.impulso.projects.repository.ProjectRepository;
import tech.impulso.projects.repository.ProjectSubmissionRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.function.Function;

/**
 * Servicio con las operaciones sobre proyectos y sus entregas
 * (RF-023 / RF-045).
 *
 * <p>Los estudiantes envían entregas incrementales; el instructor las
 * revisa y otorga calificación con retroalimentación. La primera
 * aprobación otorga XP configurable y dispara notificación.</p>
 */
@Service
public class ProjectService {

    /** Valor sentinela para desasociar vínculos opcionales en updates. */
    private static final long UNLINK_SENTINEL = -1L;

    private final ProjectRepository projectRepository;
    private final ProjectSubmissionRepository submissionRepository;
    private final LearningRouteRepository learningRouteRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final CurrentUserService currentUserService;
    private final XpService xpService;
    private final BadgeService badgeService;
    private final NotificationService notificationService;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectSubmissionRepository submissionRepository,
                          LearningRouteRepository learningRouteRepository,
                          CourseRepository courseRepository,
                          CourseModuleRepository moduleRepository,
                          CurrentUserService currentUserService,
                          XpService xpService,
                          BadgeService badgeService,
                          NotificationService notificationService) {
        this.projectRepository = projectRepository;
        this.submissionRepository = submissionRepository;
        this.learningRouteRepository = learningRouteRepository;
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.currentUserService = currentUserService;
        this.xpService = xpService;
        this.badgeService = badgeService;
        this.notificationService = notificationService;
    }

    // -------------------------------------------------------------------
    // Consultas y gestión del catálogo de proyectos
    // -------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> listPublished(String search,
                                                        DifficultyLevel difficulty,
                                                        Long learningRouteId,
                                                        Long courseId,
                                                        Long moduleId,
                                                        Pageable pageable) {
        Page<Project> page = projectRepository.search(normalize(search), difficulty,
                ContentStatus.PUBLICADO, learningRouteId, courseId, moduleId, pageable);
        return PagedResponse.from(page, ProjectResponse::from);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProjectResponse> listAll(String search,
                                                  DifficultyLevel difficulty,
                                                  ContentStatus status,
                                                  Long learningRouteId,
                                                  Long courseId,
                                                  Long moduleId,
                                                  Pageable pageable) {
        Page<Project> page = projectRepository.search(normalize(search), difficulty,
                status, learningRouteId, courseId, moduleId, pageable);
        return PagedResponse.from(page, ProjectResponse::from);
    }

    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id) {
        Project project = loadProject(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanRead(project, current);
        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse create(CreateProjectRequest request) {
        User author = currentUserService.requireAuthenticatedUser();
        ensureCanManage(author);

        Project project = new Project();
        project.setName(request.name().trim());
        project.setDescription(request.description());
        project.setObjective(request.objective());
        project.setInstructions(request.instructions());
        project.setRequirements(request.requirements());
        project.setDifficulty(request.difficulty());
        project.setTechnologies(nullIfBlank(request.technologies()));
        project.setResources(request.resources());
        project.setEvaluationCriteria(request.evaluationCriteria());
        project.setMaxScore(request.maxScore() != null ? request.maxScore() : 100);
        project.setXpReward(request.xpReward() != null ? request.xpReward() : 100);
        project.setDeadlineAt(request.deadlineAt());
        project.setStatus(ContentStatus.BORRADOR);
        project.setInstructor(author);
        assignLinks(project, request.learningRouteId(), request.courseId(), request.moduleId(), false);

        return ProjectResponse.from(projectRepository.save(project));
    }

    @Transactional
    public ProjectResponse update(Long id, UpdateProjectRequest request) {
        Project project = loadProject(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(project, current);

        if (request.name() != null) {
            project.setName(request.name().trim());
        }
        if (request.description() != null) {
            project.setDescription(request.description());
        }
        if (request.objective() != null) {
            project.setObjective(request.objective());
        }
        if (request.instructions() != null) {
            project.setInstructions(request.instructions());
        }
        if (request.requirements() != null) {
            project.setRequirements(request.requirements());
        }
        if (request.difficulty() != null) {
            project.setDifficulty(request.difficulty());
        }
        if (request.technologies() != null) {
            project.setTechnologies(nullIfBlank(request.technologies()));
        }
        if (request.resources() != null) {
            project.setResources(request.resources());
        }
        if (request.evaluationCriteria() != null) {
            project.setEvaluationCriteria(request.evaluationCriteria());
        }
        if (request.maxScore() != null) {
            project.setMaxScore(request.maxScore());
        }
        if (request.xpReward() != null) {
            project.setXpReward(request.xpReward());
        }
        if (request.deadlineAt() != null) {
            project.setDeadlineAt(request.deadlineAt());
        }
        assignLinks(project, request.learningRouteId(), request.courseId(), request.moduleId(), true);

        return ProjectResponse.from(project);
    }

    @Transactional
    public ProjectResponse changeStatus(Long id, ContentStatus status) {
        Project project = loadProject(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(project, current);
        project.setStatus(status);
        return ProjectResponse.from(project);
    }

    @Transactional
    public void delete(Long id) {
        Project project = loadProject(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(project, current);
        projectRepository.delete(project);
    }

    // -------------------------------------------------------------------
    // Entregas del estudiante
    // -------------------------------------------------------------------

    /**
     * Registra una nueva entrega del estudiante autenticado sobre el
     * proyecto indicado. Determina automáticamente el número de entrega
     * y marca la bandera de entrega tardía si aplica.
     */
    @Transactional
    public ProjectSubmissionResponse submit(Long projectId, CreateSubmissionRequest request) {
        Project project = loadProject(projectId);
        User user = currentUserService.requireAuthenticatedUser();

        if (project.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "El proyecto no está disponible para entregas.");
        }

        Integer previous = submissionRepository.findMaxSubmissionNumber(user.getId(), projectId);
        int next = previous == null ? 1 : previous + 1;

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        ProjectSubmission submission = new ProjectSubmission();
        submission.setProject(project);
        submission.setUser(user);
        submission.setSubmissionNumber(next);
        submission.setSubmissionUrl(request.submissionUrl().trim());
        submission.setStudentNotes(request.studentNotes());
        submission.setStatus(ProjectSubmissionStatus.ENVIADA);
        submission.setLateSubmission(project.getDeadlineAt() != null
                && now.isAfter(project.getDeadlineAt()));
        return ProjectSubmissionResponse.from(submissionRepository.save(submission));
    }

    /**
     * Devuelve las entregas del usuario autenticado sobre un proyecto
     * específico, ordenadas de la más reciente a la más antigua.
     */
    @Transactional(readOnly = true)
    public List<ProjectSubmissionResponse> listMySubmissions(Long projectId) {
        User user = currentUserService.requireAuthenticatedUser();
        return submissionRepository.findByUserIdAndProjectIdOrderBySubmissionNumberDesc(user.getId(), projectId).stream()
                .map(ProjectSubmissionResponse::from)
                .toList();
    }

    /**
     * Devuelve las entregas recibidas para un proyecto. Reservado al
     * instructor responsable o a un administrador.
     */
    @Transactional(readOnly = true)
    public PagedResponse<ProjectSubmissionResponse> listProjectSubmissions(Long projectId,
                                                                           ProjectSubmissionStatus status,
                                                                           Pageable pageable) {
        Project project = loadProject(projectId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(project, current);
        Page<ProjectSubmission> page = submissionRepository.findByProject(projectId, status, pageable);
        return PagedResponse.from(page, ProjectSubmissionResponse::from);
    }

    /**
     * Revisa una entrega: aplica el nuevo estado, calificación y
     * retroalimentación. Cuando la revisión es una aprobación otorga la
     * XP configurada por el instructor (idempotente) y notifica al
     * estudiante.
     */
    @Transactional
    public ProjectSubmissionResponse review(Long submissionId, ReviewSubmissionRequest request) {
        ProjectSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "La entrega indicada no existe."));
        Project project = submission.getProject();
        User reviewer = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(project, reviewer);

        if (request.status() == ProjectSubmissionStatus.ENVIADA
                || request.status() == ProjectSubmissionStatus.EN_REVISION) {
            // Estas dos transiciones ocurren automáticamente y no se
            // ofrecen desde la revisión formal.
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El estado indicado no es válido para una revisión.");
        }

        boolean alreadyApproved = submissionRepository.hasApprovedSubmission(
                submission.getUser().getId(), project.getId());

        submission.setStatus(request.status());
        submission.setGrade(request.grade());
        submission.setFeedback(request.feedback());
        submission.setReviewedBy(reviewer);
        submission.setReviewedAt(OffsetDateTime.now(ZoneOffset.UTC));

        User student = submission.getUser();
        if (request.status() == ProjectSubmissionStatus.APROBADA) {
            if (!alreadyApproved && project.getXpReward() > 0) {
                xpService.awardForProjectApproved(student, project.getId(), project.getXpReward());
                badgeService.evaluateAndAward(student);
            }
            notificationService.notify(
                    student,
                    NotificationType.GENERIC,
                    "Proyecto aprobado: %s".formatted(project.getName()),
                    "Tu entrega #%d fue aprobada por el instructor.".formatted(submission.getSubmissionNumber()),
                    "PROJECT",
                    project.getId()
            );
        } else if (request.status() == ProjectSubmissionStatus.CORRECCION_SOLICITADA) {
            notificationService.notify(
                    student,
                    NotificationType.GENERIC,
                    "Correcciones solicitadas en %s".formatted(project.getName()),
                    "El instructor solicitó ajustes en tu entrega #%d. Revisa la retroalimentación."
                            .formatted(submission.getSubmissionNumber()),
                    "PROJECT",
                    project.getId()
            );
        } else if (request.status() == ProjectSubmissionStatus.RECHAZADA) {
            notificationService.notify(
                    student,
                    NotificationType.GENERIC,
                    "Entrega rechazada en %s".formatted(project.getName()),
                    "El instructor rechazó tu entrega #%d. Revisa la retroalimentación."
                            .formatted(submission.getSubmissionNumber()),
                    "PROJECT",
                    project.getId()
            );
        }

        return ProjectSubmissionResponse.from(submission);
    }

    // -------------------------------------------------------------------
    // Utilidades internas
    // -------------------------------------------------------------------

    private void assignLinks(Project project, Long learningRouteId, Long courseId,
                             Long moduleId, boolean allowUnlink) {
        if (learningRouteId != null) {
            project.setLearningRoute(resolveOptional(learningRouteId, allowUnlink,
                    id -> learningRouteRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "La ruta de aprendizaje indicada no existe."))));
        }
        if (courseId != null) {
            project.setCourse(resolveOptional(courseId, allowUnlink,
                    id -> courseRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "El curso indicado no existe."))));
        }
        if (moduleId != null) {
            project.setModule(resolveOptional(moduleId, allowUnlink,
                    id -> moduleRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "El módulo indicado no existe."))));
        }
    }

    private <T> T resolveOptional(Long id, boolean allowUnlink, Function<Long, T> loader) {
        if (allowUnlink && id == UNLINK_SENTINEL) {
            return null;
        }
        return loader.apply(id);
    }

    private Project loadProject(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El proyecto indicado no existe."));
    }

    private void ensureCanManage(User user) {
        if (user.getRole() == Role.ESTUDIANTE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No cuenta con permisos para gestionar proyectos.");
        }
    }

    private void ensureCanEdit(Project project, User user) {
        ensureCanManage(user);
        boolean isOwner = project.getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el instructor responsable o un administrador puede modificar este proyecto.");
        }
    }

    private void ensureCanRead(Project project, User user) {
        if (project.getStatus() == ContentStatus.PUBLICADO) {
            return;
        }
        boolean isOwner = project.getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El proyecto indicado no existe.");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String nullIfBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /** Suprime warning por el uso de tipos no referenciados directamente. */
    @SuppressWarnings("unused")
    private void referenceUnused(Course c, CourseModule m, LearningRoute r) { }
}
