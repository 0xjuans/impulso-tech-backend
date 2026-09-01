package tech.impulso.resources.service;

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
import tech.impulso.learningroutes.entity.LearningRoute;
import tech.impulso.learningroutes.repository.LearningRouteRepository;
import tech.impulso.lessons.entity.Lesson;
import tech.impulso.lessons.repository.LessonRepository;
import tech.impulso.resources.dto.CreateResourceRequest;
import tech.impulso.resources.dto.EducationalResourceResponse;
import tech.impulso.resources.dto.UpdateResourceRequest;
import tech.impulso.resources.entity.EducationalResource;
import tech.impulso.resources.entity.ResourceType;
import tech.impulso.resources.repository.EducationalResourceRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Servicio con las operaciones sobre la biblioteca de recursos
 * educativos (RF-024).
 *
 * <p>Los estudiantes sólo ven recursos publicados; los instructores y
 * administradores pueden consultar y gestionar el catálogo completo. Al
 * publicar un recurso por primera vez se registra {@code publishedAt}
 * para poder ordenar por novedad.</p>
 */
@Service
public class EducationalResourceService {

    /** Valor sentinela que indica desasociar el recurso del contenido. */
    private static final long UNLINK_SENTINEL = -1L;

    private final EducationalResourceRepository repository;
    private final LearningRouteRepository learningRouteRepository;
    private final CourseRepository courseRepository;
    private final CourseModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final CurrentUserService currentUserService;

    public EducationalResourceService(EducationalResourceRepository repository,
                                      LearningRouteRepository learningRouteRepository,
                                      CourseRepository courseRepository,
                                      CourseModuleRepository moduleRepository,
                                      LessonRepository lessonRepository,
                                      CurrentUserService currentUserService) {
        this.repository = repository;
        this.learningRouteRepository = learningRouteRepository;
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.lessonRepository = lessonRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Lista los recursos publicados visibles para cualquier usuario
     * autenticado.
     */
    @Transactional(readOnly = true)
    public PagedResponse<EducationalResourceResponse> listPublished(String search,
                                                                    ResourceType type,
                                                                    DifficultyLevel difficulty,
                                                                    Long learningRouteId,
                                                                    Long courseId,
                                                                    Long moduleId,
                                                                    Long lessonId,
                                                                    Pageable pageable) {
        Page<EducationalResource> page = repository.search(normalize(search), type, difficulty,
                ContentStatus.PUBLICADO, learningRouteId, courseId, moduleId, lessonId, pageable);
        return PagedResponse.from(page, EducationalResourceResponse::from);
    }

    /**
     * Lista todos los recursos incluyendo borradores y deshabilitados
     * para vistas administrativas y de gestión.
     */
    @Transactional(readOnly = true)
    public PagedResponse<EducationalResourceResponse> listAll(String search,
                                                              ResourceType type,
                                                              DifficultyLevel difficulty,
                                                              ContentStatus status,
                                                              Long learningRouteId,
                                                              Long courseId,
                                                              Long moduleId,
                                                              Long lessonId,
                                                              Pageable pageable) {
        Page<EducationalResource> page = repository.search(normalize(search), type, difficulty,
                status, learningRouteId, courseId, moduleId, lessonId, pageable);
        return PagedResponse.from(page, EducationalResourceResponse::from);
    }

    /**
     * Consulta el detalle de un recurso.
     */
    @Transactional(readOnly = true)
    public EducationalResourceResponse findById(Long id) {
        EducationalResource resource = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanRead(resource, current);
        return EducationalResourceResponse.from(resource);
    }

    /**
     * Crea un nuevo recurso educativo en estado {@code BORRADOR}.
     */
    @Transactional
    public EducationalResourceResponse create(CreateResourceRequest request) {
        User author = currentUserService.requireAuthenticatedUser();
        ensureCanManage(author);

        EducationalResource resource = new EducationalResource();
        resource.setName(request.name().trim());
        resource.setDescription(request.description());
        resource.setType(request.type());
        resource.setCategory(nullIfBlank(request.category()));
        resource.setTopic(nullIfBlank(request.topic()));
        resource.setTechnology(nullIfBlank(request.technology()));
        resource.setDifficulty(request.difficulty());
        resource.setAuthor(nullIfBlank(request.author()));
        resource.setResourceUrl(request.resourceUrl().trim());
        resource.setStatus(ContentStatus.BORRADOR);
        resource.setCreatedBy(author);
        assignLinks(resource, request.learningRouteId(), request.courseId(),
                request.moduleId(), request.lessonId(), false);

        return EducationalResourceResponse.from(repository.save(resource));
    }

    /**
     * Actualiza los campos permitidos de un recurso existente.
     */
    @Transactional
    public EducationalResourceResponse update(Long id, UpdateResourceRequest request) {
        EducationalResource resource = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(resource, current);

        if (request.name() != null) {
            resource.setName(request.name().trim());
        }
        if (request.description() != null) {
            resource.setDescription(request.description());
        }
        if (request.type() != null) {
            resource.setType(request.type());
        }
        if (request.category() != null) {
            resource.setCategory(nullIfBlank(request.category()));
        }
        if (request.topic() != null) {
            resource.setTopic(nullIfBlank(request.topic()));
        }
        if (request.technology() != null) {
            resource.setTechnology(nullIfBlank(request.technology()));
        }
        if (request.difficulty() != null) {
            resource.setDifficulty(request.difficulty());
        }
        if (request.author() != null) {
            resource.setAuthor(nullIfBlank(request.author()));
        }
        if (request.resourceUrl() != null) {
            resource.setResourceUrl(request.resourceUrl().trim());
        }
        assignLinks(resource, request.learningRouteId(), request.courseId(),
                request.moduleId(), request.lessonId(), true);

        return EducationalResourceResponse.from(resource);
    }

    /**
     * Cambia el estado del ciclo de vida del recurso. La primera vez
     * que se publica se registra {@code publishedAt}.
     */
    @Transactional
    public EducationalResourceResponse changeStatus(Long id, ContentStatus status) {
        EducationalResource resource = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(resource, current);
        resource.setStatus(status);
        if (status == ContentStatus.PUBLICADO && resource.getPublishedAt() == null) {
            resource.setPublishedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        return EducationalResourceResponse.from(resource);
    }

    /**
     * Elimina el recurso indicado.
     */
    @Transactional
    public void delete(Long id) {
        EducationalResource resource = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(resource, current);
        repository.delete(resource);
    }

    /**
     * Asigna o desasigna los vínculos con ruta, curso, módulo y
     * lección. Cuando se actualiza un recurso existente se admite el
     * valor sentinela {@code -1} para desasociar.
     *
     * @param resource        recurso a modificar.
     * @param learningRouteId ruta o {@code null} / {@code -1} según flag.
     * @param courseId        curso o {@code null} / {@code -1} según flag.
     * @param moduleId        módulo o {@code null} / {@code -1} según flag.
     * @param lessonId        lección o {@code null} / {@code -1} según flag.
     * @param allowUnlink     indica si el sentinela debe interpretarse.
     */
    private void assignLinks(EducationalResource resource, Long learningRouteId, Long courseId,
                             Long moduleId, Long lessonId, boolean allowUnlink) {
        if (learningRouteId != null) {
            resource.setLearningRoute(resolveOptional(learningRouteId, allowUnlink,
                    id -> learningRouteRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "La ruta de aprendizaje indicada no existe."))));
        }
        if (courseId != null) {
            resource.setCourse(resolveOptional(courseId, allowUnlink,
                    id -> courseRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "El curso indicado no existe."))));
        }
        if (moduleId != null) {
            resource.setModule(resolveOptional(moduleId, allowUnlink,
                    id -> moduleRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "El módulo indicado no existe."))));
        }
        if (lessonId != null) {
            resource.setLesson(resolveOptional(lessonId, allowUnlink,
                    id -> lessonRepository.findById(id)
                            .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                                    "La lección indicada no existe."))));
        }
    }

    /**
     * Interpreta el identificador según el flag de desasociación.
     *
     * @param id           identificador recibido.
     * @param allowUnlink  cuando es {@code true} el sentinela {@code -1}
     *                     equivale a desasociar (retornar {@code null}).
     * @param loader       función para cargar la entidad.
     * @return la entidad correspondiente o {@code null}.
     */
    private <T> T resolveOptional(Long id, boolean allowUnlink, java.util.function.Function<Long, T> loader) {
        if (allowUnlink && id == UNLINK_SENTINEL) {
            return null;
        }
        return loader.apply(id);
    }

    private EducationalResource load(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El recurso indicado no existe."));
    }

    private void ensureCanManage(User user) {
        if (user.getRole() == Role.ESTUDIANTE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No cuenta con permisos para gestionar recursos.");
        }
    }

    private void ensureCanEdit(EducationalResource resource, User user) {
        ensureCanManage(user);
        boolean isOwner = resource.getCreatedBy().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el creador del recurso o un administrador puede modificarlo.");
        }
    }

    private void ensureCanRead(EducationalResource resource, User user) {
        if (resource.getStatus() == ContentStatus.PUBLICADO) {
            return;
        }
        boolean isOwner = resource.getCreatedBy().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El recurso indicado no existe.");
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
}
