package tech.impulso.search.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.courses.entity.Course;
import tech.impulso.courses.repository.CourseRepository;
import tech.impulso.learningroutes.entity.LearningRoute;
import tech.impulso.learningroutes.repository.LearningRouteRepository;
import tech.impulso.lessons.entity.Lesson;
import tech.impulso.lessons.repository.LessonRepository;
import tech.impulso.search.dto.SearchResponse;
import tech.impulso.search.dto.SearchResultItem;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.repository.UserRepository;

import java.util.List;
import java.util.Set;

/**
 * Servicio que atiende las búsquedas globales de la plataforma
 * (RF-035).
 *
 * <p>Consulta en paralelo cursos, rutas de aprendizaje, lecciones y
 * usuarios aplicando las reglas de visibilidad por rol:</p>
 *
 * <ul>
 *     <li>{@code ESTUDIANTE}: sólo contenido {@code PUBLICADO} y
 *         usuarios activos (excluye administradores).</li>
 *     <li>{@code INSTRUCTOR}: contenido {@code PUBLICADO} más el
 *         contenido propio en cualquier estado; usuarios activos
 *         (excluye administradores).</li>
 *     <li>{@code ADMINISTRADOR}: acceso irrestricto a todos los tipos y
 *         estados.</li>
 * </ul>
 */
@Service
public class SearchService {

    /** Longitud mínima aceptada del término de búsqueda. */
    private static final int MIN_QUERY_LENGTH = 2;

    /** Longitud máxima aceptada del término de búsqueda. */
    private static final int MAX_QUERY_LENGTH = 100;

    /** Cantidad mínima de resultados por tipo. */
    private static final int MIN_LIMIT = 1;

    /** Cantidad máxima de resultados por tipo. */
    private static final int MAX_LIMIT = 20;

    /** Cantidad de resultados por tipo cuando el cliente no especifica. */
    private static final int DEFAULT_LIMIT = 5;

    /** Identificadores válidos aceptados en el parámetro {@code types}. */
    static final Set<String> AVAILABLE_TYPES = Set.of("courses", "routes", "lessons", "users");

    private final CourseRepository courseRepository;
    private final LearningRouteRepository routeRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public SearchService(CourseRepository courseRepository,
                         LearningRouteRepository routeRepository,
                         LessonRepository lessonRepository,
                         UserRepository userRepository,
                         CurrentUserService currentUserService) {
        this.courseRepository = courseRepository;
        this.routeRepository = routeRepository;
        this.lessonRepository = lessonRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Ejecuta una búsqueda global y devuelve los resultados agrupados
     * por tipo.
     *
     * @param query término de búsqueda proporcionado por el cliente.
     * @param types conjunto de tipos solicitados; cuando es nulo o vacío
     *              se buscan todos los tipos disponibles.
     * @param limit cantidad máxima de resultados por tipo. Se acota al
     *              rango permitido y se aplica un valor por defecto
     *              cuando es nulo.
     * @return resultados agregados por tipo.
     */
    @Transactional(readOnly = true)
    public SearchResponse search(String query, Set<String> types, Integer limit) {
        String normalized = normalize(query);
        Set<String> selectedTypes = resolveTypes(types);
        Pageable pageable = PageRequest.of(0, resolveLimit(limit), Sort.by(Sort.Direction.ASC, "id"));

        User caller = currentUserService.requireAuthenticatedUser();
        boolean isAdmin = caller.getRole() == Role.ADMINISTRADOR;
        boolean isInstructor = caller.getRole() == Role.INSTRUCTOR;
        Long ownerId = isInstructor ? caller.getId() : null;

        List<SearchResultItem> courses = selectedTypes.contains("courses")
                ? courseRepository.globalSearch(normalized, ownerId, isAdmin, pageable)
                        .stream().map(SearchService::toCourseItem).toList()
                : List.of();

        List<SearchResultItem> routes = selectedTypes.contains("routes")
                ? routeRepository.globalSearch(normalized, ownerId, isAdmin, pageable)
                        .stream().map(SearchService::toRouteItem).toList()
                : List.of();

        List<SearchResultItem> lessons = selectedTypes.contains("lessons")
                ? lessonRepository.globalSearch(normalized, ownerId, isAdmin, pageable)
                        .stream().map(SearchService::toLessonItem).toList()
                : List.of();

        List<SearchResultItem> users = selectedTypes.contains("users")
                ? userRepository.globalSearch(normalized, isAdmin, pageable)
                        .stream().map(SearchService::toUserItem).toList()
                : List.of();

        return new SearchResponse(normalized, courses, routes, lessons, users);
    }

    /**
     * Recorta y valida el término de búsqueda. Rechaza términos vacíos o
     * fuera del rango permitido para evitar consultas triviales que
     * saturen la base de datos.
     */
    private static String normalize(String query) {
        if (query == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El término de búsqueda es obligatorio.");
        }
        String trimmed = query.trim();
        if (trimmed.length() < MIN_QUERY_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El término de búsqueda debe tener al menos %d caracteres.".formatted(MIN_QUERY_LENGTH));
        }
        if (trimmed.length() > MAX_QUERY_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El término de búsqueda no puede superar %d caracteres.".formatted(MAX_QUERY_LENGTH));
        }
        return trimmed;
    }

    /**
     * Determina el conjunto de tipos sobre los que realizar la búsqueda,
     * validando que cada valor recibido sea reconocido.
     */
    private static Set<String> resolveTypes(Set<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return AVAILABLE_TYPES;
        }
        for (String type : requested) {
            if (!AVAILABLE_TYPES.contains(type)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "Tipo de búsqueda no reconocido: %s".formatted(type));
            }
        }
        return requested;
    }

    /** Acota el {@code limit} al rango permitido, aplicando el valor por defecto. */
    private static int resolveLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_LIMIT;
        }
        if (limit < MIN_LIMIT) {
            return MIN_LIMIT;
        }
        if (limit > MAX_LIMIT) {
            return MAX_LIMIT;
        }
        return limit;
    }

    private static SearchResultItem toCourseItem(Course course) {
        return new SearchResultItem(
                course.getId(),
                "COURSE",
                course.getName(),
                course.getDescription(),
                course.getCoverImageUrl(),
                course.getStatus() == null ? null : course.getStatus().name(),
                course.getLearningRoute() == null ? null : course.getLearningRoute().getId()
        );
    }

    private static SearchResultItem toRouteItem(LearningRoute route) {
        return new SearchResultItem(
                route.getId(),
                "ROUTE",
                route.getName(),
                route.getDescription(),
                route.getCoverImageUrl(),
                route.getStatus() == null ? null : route.getStatus().name(),
                null
        );
    }

    private static SearchResultItem toLessonItem(Lesson lesson) {
        Long courseId = lesson.getModule() == null || lesson.getModule().getCourse() == null
                ? null
                : lesson.getModule().getCourse().getId();
        return new SearchResultItem(
                lesson.getId(),
                "LESSON",
                lesson.getTitle(),
                lesson.getDescription(),
                null,
                lesson.getStatus() == null ? null : lesson.getStatus().name(),
                courseId
        );
    }

    private static SearchResultItem toUserItem(User user) {
        String fullName = (user.getFirstName() + " " + user.getLastName()).trim();
        return new SearchResultItem(
                user.getId(),
                "USER",
                user.getUsername(),
                fullName.isEmpty() ? null : fullName,
                user.getProfilePhotoUrl(),
                user.getRole() == null ? null : user.getRole().name(),
                null
        );
    }
}
