package tech.impulso.admin.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.AdminUserResponse;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;
import tech.impulso.users.repository.UserRepository;

/**
 * Servicio con las operaciones administrativas sobre las cuentas de
 * usuario (RF-030 / RF-031).
 *
 * <p>Aplica las reglas de seguridad definidas por la plataforma:</p>
 *
 * <ul>
 *   <li>Un administrador no puede modificar su propio rol ni su propio
 *       estado, para impedir la pérdida involuntaria del acceso
 *       administrativo.</li>
 *   <li>Las cuentas nunca se eliminan definitivamente; sólo se activan o
 *       desactivan, conservando toda su información histórica.</li>
 * </ul>
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AdminUserService(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Realiza una búsqueda paginada de usuarios aplicando filtros
     * opcionales.
     *
     * @param search   fragmento para buscar en correo, nombre de usuario,
     *                 nombre o apellido.
     * @param role     rol al que restringir los resultados.
     * @param status   estado al que restringir los resultados.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con los usuarios coincidentes.
     */
    @Transactional(readOnly = true)
    public PagedResponse<AdminUserResponse> search(String search,
                                                   Role role,
                                                   UserStatus status,
                                                   Pageable pageable) {
        String normalized = (search != null && !search.isBlank()) ? search.trim() : null;
        Page<User> page = userRepository.search(normalized, role, status, pageable);
        return PagedResponse.from(page, AdminUserResponse::from);
    }

    /**
     * Devuelve la información detallada de un usuario.
     *
     * @param userId identificador del usuario a consultar.
     * @return información administrativa del usuario.
     */
    @Transactional(readOnly = true)
    public AdminUserResponse findById(Long userId) {
        return AdminUserResponse.from(loadUser(userId));
    }

    /**
     * Actualiza el rol de un usuario. Impide que el administrador
     * autenticado modifique su propio rol.
     *
     * @param userId identificador del usuario a modificar.
     * @param newRole nuevo rol a asignar.
     * @return información actualizada del usuario.
     */
    @Transactional
    public AdminUserResponse changeRole(Long userId, Role newRole) {
        User target = loadUser(userId);
        User admin = currentUserService.requireAuthenticatedUser();

        if (target.getId().equals(admin.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Un administrador no puede modificar su propio rol.");
        }

        target.setRole(newRole);
        userRepository.save(target);
        return AdminUserResponse.from(target);
    }

    /**
     * Actualiza el estado de una cuenta (activación / desactivación).
     * No permite pasar una cuenta al estado
     * {@link UserStatus#PENDIENTE_VERIFICACION} ni modificar la propia
     * cuenta administrativa.
     *
     * @param userId identificador del usuario a modificar.
     * @param newStatus nuevo estado que se aplicará.
     * @return información actualizada del usuario.
     */
    @Transactional
    public AdminUserResponse changeStatus(Long userId, UserStatus newStatus) {
        if (newStatus == UserStatus.PENDIENTE_VERIFICACION) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Una cuenta no puede regresar al estado pendiente de verificación desde el panel administrativo.");
        }

        User target = loadUser(userId);
        User admin = currentUserService.requireAuthenticatedUser();

        if (target.getId().equals(admin.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Un administrador no puede modificar su propio estado de cuenta.");
        }

        target.setStatus(newStatus);
        userRepository.save(target);
        return AdminUserResponse.from(target);
    }

    /**
     * Localiza al usuario o lanza una excepción de negocio con código 404.
     *
     * @param userId identificador buscado.
     * @return el usuario encontrado.
     */
    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El usuario indicado no existe."));
    }
}
