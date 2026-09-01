package tech.impulso.learningroutes.service;

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
import tech.impulso.learningroutes.dto.CreateLearningRouteRequest;
import tech.impulso.learningroutes.dto.LearningRouteResponse;
import tech.impulso.learningroutes.dto.UpdateLearningRouteRequest;
import tech.impulso.learningroutes.entity.LearningRoute;
import tech.impulso.learningroutes.repository.LearningRouteRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

/**
 * Servicio con las operaciones sobre rutas de aprendizaje (RF-039).
 *
 * <p>Combina las funcionalidades disponibles para estudiantes (consulta
 * de rutas publicadas) y para instructores y administradores (creación,
 * edición y control del ciclo de vida). Las validaciones de autorización
 * comprueban que el instructor sólo pueda modificar las rutas que él
 * mismo administra.</p>
 */
@Service
public class LearningRouteService {

    private final LearningRouteRepository repository;
    private final CurrentUserService currentUserService;

    public LearningRouteService(LearningRouteRepository repository,
                                CurrentUserService currentUserService) {
        this.repository = repository;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve las rutas publicadas visibles para cualquier usuario.
     *
     * @param search     fragmento a buscar en nombre o descripción.
     * @param difficulty nivel de dificultad opcional.
     * @param pageable   configuración de paginación y ordenamiento.
     * @return página con las rutas publicadas coincidentes.
     */
    @Transactional(readOnly = true)
    public PagedResponse<LearningRouteResponse> listPublished(String search,
                                                              DifficultyLevel difficulty,
                                                              Pageable pageable) {
        Page<LearningRoute> page = repository.search(normalize(search), difficulty, ContentStatus.PUBLICADO, pageable);
        return PagedResponse.from(page, LearningRouteResponse::from);
    }

    /**
     * Devuelve rutas sin restricción de estado. Se utiliza en las vistas
     * administrativas y de gestión donde se necesitan también las rutas
     * en borrador o deshabilitadas.
     *
     * @param search     fragmento a buscar.
     * @param difficulty nivel de dificultad opcional.
     * @param status     estado opcional.
     * @param pageable   configuración de paginación y ordenamiento.
     * @return página con las rutas coincidentes.
     */
    @Transactional(readOnly = true)
    public PagedResponse<LearningRouteResponse> listAll(String search,
                                                        DifficultyLevel difficulty,
                                                        ContentStatus status,
                                                        Pageable pageable) {
        Page<LearningRoute> page = repository.search(normalize(search), difficulty, status, pageable);
        return PagedResponse.from(page, LearningRouteResponse::from);
    }

    /**
     * Consulta el detalle de una ruta.
     *
     * <p>Los estudiantes sólo pueden acceder a rutas publicadas; los
     * instructores dueños de la ruta y los administradores pueden
     * acceder en cualquier estado.</p>
     *
     * @param id identificador de la ruta.
     * @return información pública de la ruta.
     */
    @Transactional(readOnly = true)
    public LearningRouteResponse findById(Long id) {
        LearningRoute route = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanRead(route, current);
        return LearningRouteResponse.from(route);
    }

    /**
     * Crea una nueva ruta. El autor pasa a ser el usuario autenticado.
     *
     * @param request datos suministrados por el cliente.
     * @return ruta creada.
     */
    @Transactional
    public LearningRouteResponse create(CreateLearningRouteRequest request) {
        User author = currentUserService.requireAuthenticatedUser();
        ensureCanManage(author);

        LearningRoute route = new LearningRoute();
        route.setName(request.name().trim());
        route.setDescription(request.description());
        route.setObjective(request.objective());
        route.setCoverImageUrl(nullIfBlank(request.coverImageUrl()));
        route.setDifficulty(request.difficulty());
        route.setEstimatedDurationHours(request.estimatedDurationHours());
        route.setTechnologies(nullIfBlank(request.technologies()));
        route.setStatus(ContentStatus.BORRADOR);
        route.setInstructor(author);

        return LearningRouteResponse.from(repository.save(route));
    }

    /**
     * Actualiza los campos permitidos de una ruta.
     *
     * @param id      identificador de la ruta a modificar.
     * @param request valores a actualizar.
     * @return ruta actualizada.
     */
    @Transactional
    public LearningRouteResponse update(Long id, UpdateLearningRouteRequest request) {
        LearningRoute route = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(route, current);

        if (request.name() != null) {
            route.setName(request.name().trim());
        }
        if (request.description() != null) {
            route.setDescription(request.description());
        }
        if (request.objective() != null) {
            route.setObjective(request.objective());
        }
        if (request.coverImageUrl() != null) {
            route.setCoverImageUrl(nullIfBlank(request.coverImageUrl()));
        }
        if (request.difficulty() != null) {
            route.setDifficulty(request.difficulty());
        }
        if (request.estimatedDurationHours() != null) {
            route.setEstimatedDurationHours(request.estimatedDurationHours());
        }
        if (request.technologies() != null) {
            route.setTechnologies(nullIfBlank(request.technologies()));
        }

        return LearningRouteResponse.from(route);
    }

    /**
     * Cambia el estado del ciclo de vida de la ruta.
     *
     * @param id     identificador de la ruta.
     * @param status nuevo estado.
     * @return ruta actualizada.
     */
    @Transactional
    public LearningRouteResponse changeStatus(Long id, ContentStatus status) {
        LearningRoute route = load(id);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEdit(route, current);
        route.setStatus(status);
        return LearningRouteResponse.from(route);
    }

    /**
     * Carga una ruta por identificador o lanza 404.
     *
     * @param id identificador buscado.
     * @return la ruta encontrada.
     */
    private LearningRoute load(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La ruta de aprendizaje indicada no existe."));
    }

    /**
     * Verifica que el usuario tenga rol de instructor o administrador
     * para operaciones de gestión.
     *
     * @param user usuario autenticado.
     */
    private void ensureCanManage(User user) {
        if (user.getRole() == Role.ESTUDIANTE) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "No cuenta con permisos para gestionar rutas de aprendizaje.");
        }
    }

    /**
     * Verifica que el usuario pueda editar la ruta indicada.
     *
     * @param route ruta a modificar.
     * @param user  usuario autenticado.
     */
    private void ensureCanEdit(LearningRoute route, User user) {
        ensureCanManage(user);
        boolean isOwner = route.getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Solo el instructor responsable o un administrador puede modificar esta ruta.");
        }
    }

    /**
     * Verifica que el usuario pueda visualizar la ruta indicada. Un
     * estudiante sólo puede ver rutas publicadas.
     *
     * @param route ruta a consultar.
     * @param user  usuario autenticado.
     */
    private void ensureCanRead(LearningRoute route, User user) {
        if (route.getStatus() == ContentStatus.PUBLICADO) {
            return;
        }
        boolean isOwner = route.getInstructor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "La ruta de aprendizaje indicada no existe.");
        }
    }

    /** Normaliza cadenas eliminando vacíos y espacios superfluos. */
    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /** Devuelve {@code null} cuando el valor está vacío o en blanco. */
    private String nullIfBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
