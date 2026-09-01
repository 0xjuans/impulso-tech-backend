package tech.impulso.common.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.users.entity.User;
import tech.impulso.users.repository.UserRepository;

/**
 * Servicio auxiliar que expone al usuario autenticado en la petición
 * actual.
 *
 * <p>Actúa como puente entre el {@link SecurityContextHolder} de Spring
 * Security y la entidad de dominio {@link User}. Encapsula la búsqueda
 * para que los servicios y controladores no dependan directamente del
 * contexto de seguridad.</p>
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Devuelve el usuario autenticado en la petición en curso.
     *
     * @return el usuario asociado a la sesión.
     * @throws BusinessException con código 401 si no hay una autenticación
     *         válida en el contexto de seguridad.
     */
    public User requireAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para acceder a este recurso.");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "La sesión actual no está asociada a un usuario válido."));
    }
}
