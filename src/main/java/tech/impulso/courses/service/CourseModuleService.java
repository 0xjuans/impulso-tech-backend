package tech.impulso.courses.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.dto.CourseModuleResponse;
import tech.impulso.courses.dto.CreateCourseModuleRequest;
import tech.impulso.courses.dto.UpdateCourseModuleRequest;
import tech.impulso.courses.entity.Course;
import tech.impulso.courses.entity.CourseModule;
import tech.impulso.courses.repository.CourseModuleRepository;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.util.List;

/**
 * Servicio con las operaciones sobre módulos de curso (RF-041).
 *
 * <p>Cubre la consulta pública de los módulos de un curso (limitada a
 * los publicados cuando el usuario es estudiante) y la gestión completa
 * por parte del instructor responsable o de un administrador.</p>
 */
@Service
public class CourseModuleService {

    private final CourseModuleRepository moduleRepository;
    private final CourseRepository courseRepository;
    private final CurrentUserService currentUserService;

    public CourseModuleService(CourseModuleRepository moduleRepository,
                               CourseRepository courseRepository,
                               CurrentUserService currentUserService) {
        this.moduleRepository = moduleRepository;
        this.courseRepository = courseRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve los módulos del curso indicado ordenados por posición.
     *
     * <p>Los estudiantes sólo verán los módulos publicados; el instructor
     * responsable y los administradores verán la totalidad.</p>
     *
     * @param courseId identificador del curso.
     * @return módulos coincidentes.
     */
    @Transactional(readOnly = true)
    public List<CourseModuleResponse> listByCourse(Long courseId) {
        Course course = loadCourse(courseId);
        User current = currentUserService.requireAuthenticatedUser();
        ContentStatus filter = canManage(course, current) ? null : ContentStatus.PUBLICADO;
        if (filter == ContentStatus.PUBLICADO && course.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El curso indicado no existe.");
        }
        return moduleRepository.findByCourse(courseId, filter).stream()
                .map(CourseModuleResponse::from)
                .toList();
    }

    /**
     * Consulta un módulo por identificador aplicando las reglas de
     * visibilidad correspondientes.
     */
    @Transactional(readOnly = true)
    public CourseModuleResponse findById(Long moduleId) {
        CourseModule module = loadModule(moduleId);
        User current = currentUserService.requireAuthenticatedUser();
        if (!canManage(module.getCourse(), current)
                && module.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "El módulo indicado no existe.");
        }
        return CourseModuleResponse.from(module);
    }

    /**
     * Crea un nuevo módulo dentro del curso indicado.
     */
    @Transactional
    public CourseModuleResponse create(Long courseId, CreateCourseModuleRequest request) {
        Course course = loadCourse(courseId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(course, current);

        int orderIndex = request.orderIndex() != null
                ? request.orderIndex()
                : nextOrderIndex(courseId);
        ensureOrderIndexAvailable(courseId, orderIndex, null);

        CourseModule module = new CourseModule();
        module.setCourse(course);
        module.setName(request.name().trim());
        module.setDescription(request.description());
        module.setObjective(request.objective());
        module.setOrderIndex(orderIndex);
        module.setOptional(request.optional());
        module.setStatus(ContentStatus.BORRADOR);

        return CourseModuleResponse.from(moduleRepository.save(module));
    }

    /**
     * Actualiza los campos permitidos de un módulo.
     */
    @Transactional
    public CourseModuleResponse update(Long moduleId, UpdateCourseModuleRequest request) {
        CourseModule module = loadModule(moduleId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(module.getCourse(), current);

        if (request.name() != null) {
            module.setName(request.name().trim());
        }
        if (request.description() != null) {
            module.setDescription(request.description());
        }
        if (request.objective() != null) {
            module.setObjective(request.objective());
        }
        if (request.optional() != null) {
            module.setOptional(request.optional());
        }
        if (request.orderIndex() != null && !request.orderIndex().equals(module.getOrderIndex())) {
            ensureOrderIndexAvailable(module.getCourse().getId(), request.orderIndex(), moduleId);
            module.setOrderIndex(request.orderIndex());
        }

        return CourseModuleResponse.from(module);
    }

    /**
     * Cambia el estado del ciclo de vida de un módulo.
     */
    @Transactional
    public CourseModuleResponse changeStatus(Long moduleId, ContentStatus status) {
        CourseModule module = loadModule(moduleId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(module.getCourse(), current);
        module.setStatus(status);
        return CourseModuleResponse.from(module);
    }

    /**
     * Elimina un módulo del curso. Sus lecciones y contenidos asociados
     * se eliminan en cascada por la base de datos.
     */
    @Transactional
    public void delete(Long moduleId) {
        CourseModule module = loadModule(moduleId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(module.getCourse(), current);
        moduleRepository.delete(module);
    }

    private int nextOrderIndex(Long courseId) {
        Integer current = moduleRepository.findMaxOrderIndex(courseId);
        return current == null ? 1 : current + 1;
    }

    private void ensureOrderIndexAvailable(Long courseId, Integer orderIndex, Long ignoredModuleId) {
        moduleRepository.findByCourse(courseId, null).stream()
                .filter(existing -> existing.getOrderIndex().equals(orderIndex))
                .filter(existing -> ignoredModuleId == null || !existing.getId().equals(ignoredModuleId))
                .findAny()
                .ifPresent(conflict -> {
                    throw new BusinessException(HttpStatus.CONFLICT,
                            "Ya existe un módulo en la posición %d dentro del curso.".formatted(orderIndex));
                });
    }

    private Course loadCourse(Long courseId) {
        return courseRepository.findById(courseId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El curso indicado no existe."));
    }

    private CourseModule loadModule(Long moduleId) {
        return moduleRepository.findById(moduleId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El módulo indicado no existe."));
    }

    private boolean canManage(Course course, User user) {
        if (user.getRole() == Role.ADMINISTRADOR) {
            return true;
        }
        return user.getRole() != Role.ESTUDIANTE
                && course.getInstructor().getId().equals(user.getId());
    }

    private void ensureCanEdit(Course course, User user) {
        if (!canManage(course, user)) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el instructor responsable o un administrador puede modificar los módulos de este curso.");
        }
    }
}
