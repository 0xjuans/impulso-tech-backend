package tech.impulso.lessons.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.entity.CourseModule;
import tech.impulso.courses.repository.CourseModuleRepository;
import tech.impulso.lessons.dto.CreateLessonRequest;
import tech.impulso.lessons.dto.LessonResponse;
import tech.impulso.lessons.dto.UpdateLessonRequest;
import tech.impulso.lessons.entity.Lesson;
import tech.impulso.lessons.repository.LessonRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.util.List;

/**
 * Servicio con las operaciones sobre lecciones (RF-041).
 *
 * <p>Cubre la consulta pública de las lecciones publicadas de un módulo y
 * la gestión completa por parte del instructor responsable del curso al
 * que pertenece el módulo, o de un administrador.</p>
 */
@Service
public class LessonService {

    private final LessonRepository lessonRepository;
    private final CourseModuleRepository moduleRepository;
    private final CurrentUserService currentUserService;

    public LessonService(LessonRepository lessonRepository,
                         CourseModuleRepository moduleRepository,
                         CurrentUserService currentUserService) {
        this.lessonRepository = lessonRepository;
        this.moduleRepository = moduleRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve las lecciones del módulo indicado ordenadas por su
     * posición. Los estudiantes sólo verán las publicadas; el instructor
     * responsable y los administradores verán la totalidad.
     */
    @Transactional(readOnly = true)
    public List<LessonResponse> listByModule(Long moduleId) {
        CourseModule module = loadModule(moduleId);
        User current = currentUserService.requireAuthenticatedUser();
        ContentStatus filter = canManage(module, current) ? null : ContentStatus.PUBLICADO;
        if (filter == ContentStatus.PUBLICADO && module.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El módulo indicado no existe.");
        }
        return lessonRepository.findByModule(moduleId, filter).stream()
                .map(LessonResponse::from)
                .toList();
    }

    /**
     * Consulta una lección por identificador aplicando las reglas de
     * visibilidad correspondientes.
     */
    @Transactional(readOnly = true)
    public LessonResponse findById(Long lessonId) {
        Lesson lesson = loadLesson(lessonId);
        User current = currentUserService.requireAuthenticatedUser();
        if (!canManage(lesson.getModule(), current)
                && lesson.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "La lección indicada no existe.");
        }
        return LessonResponse.from(lesson);
    }

    /**
     * Crea una nueva lección dentro del módulo indicado.
     */
    @Transactional
    public LessonResponse create(Long moduleId, CreateLessonRequest request) {
        CourseModule module = loadModule(moduleId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(module, current);

        int orderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : nextOrderIndex(moduleId);
        ensureOrderIndexAvailable(moduleId, orderIndex, null);

        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(request.title().trim());
        lesson.setDescription(request.description());
        lesson.setObjective(request.objective());
        lesson.setContent(request.content());
        lesson.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        lesson.setOrderIndex(orderIndex);
        lesson.setOptional(request.optional());
        lesson.setStatus(ContentStatus.BORRADOR);

        return LessonResponse.from(lessonRepository.save(lesson));
    }

    /**
     * Actualiza los campos permitidos de una lección.
     */
    @Transactional
    public LessonResponse update(Long lessonId, UpdateLessonRequest request) {
        Lesson lesson = loadLesson(lessonId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(lesson.getModule(), current);

        if (request.title() != null) {
            lesson.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            lesson.setDescription(request.description());
        }
        if (request.objective() != null) {
            lesson.setObjective(request.objective());
        }
        if (request.content() != null) {
            lesson.setContent(request.content());
        }
        if (request.estimatedDurationMinutes() != null) {
            lesson.setEstimatedDurationMinutes(request.estimatedDurationMinutes());
        }
        if (request.optional() != null) {
            lesson.setOptional(request.optional());
        }
        if (request.orderIndex() != null && !request.orderIndex().equals(lesson.getOrderIndex())) {
            ensureOrderIndexAvailable(lesson.getModule().getId(), request.orderIndex(), lessonId);
            lesson.setOrderIndex(request.orderIndex());
        }

        return LessonResponse.from(lesson);
    }

    /**
     * Cambia el estado del ciclo de vida de una lección.
     */
    @Transactional
    public LessonResponse changeStatus(Long lessonId, ContentStatus status) {
        Lesson lesson = loadLesson(lessonId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(lesson.getModule(), current);
        lesson.setStatus(status);
        return LessonResponse.from(lesson);
    }

    /**
     * Elimina una lección del módulo.
     */
    @Transactional
    public void delete(Long lessonId) {
        Lesson lesson = loadLesson(lessonId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(lesson.getModule(), current);
        lessonRepository.delete(lesson);
    }

    private int nextOrderIndex(Long moduleId) {
        Integer current = lessonRepository.findMaxOrderIndex(moduleId);
        return current == null ? 1 : current + 1;
    }

    private void ensureOrderIndexAvailable(Long moduleId, Integer orderIndex, Long ignoredLessonId) {
        lessonRepository.findByModule(moduleId, null).stream()
                .filter(existing -> existing.getOrderIndex().equals(orderIndex))
                .filter(existing -> ignoredLessonId == null || !existing.getId().equals(ignoredLessonId))
                .findAny()
                .ifPresent(conflict -> {
                    throw new BusinessException(HttpStatus.CONFLICT,
                            "Ya existe una lección en la posición %d dentro del módulo.".formatted(orderIndex));
                });
    }

    private CourseModule loadModule(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El módulo indicado no existe."));
    }

    private Lesson loadLesson(Long lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La lección indicada no existe."));
    }

    private boolean canManage(CourseModule module, User user) {
        if (user.getRole() == Role.ADMINISTRADOR) {
            return true;
        }
        return user.getRole() != Role.ESTUDIANTE
                && module.getCourse().getInstructor().getId().equals(user.getId());
    }

    private void ensureCanEdit(CourseModule module, User user) {
        if (!canManage(module, user)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el instructor responsable o un administrador puede modificar las lecciones de este módulo.");
        }
    }
}
