package tech.impulso.users.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.auth.dto.UserResponse;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.users.dto.ChangePasswordRequest;
import tech.impulso.users.dto.UpdateProfileRequest;
import tech.impulso.users.entity.User;
import tech.impulso.users.repository.UserRepository;

/**
 * Servicio encargado de las operaciones que el usuario autenticado puede
 * realizar sobre su propio perfil (RF-005).
 *
 * <p>Contempla la consulta de la información personal, la actualización
 * de los campos permitidos y el cambio de contraseña. El rol y el estado
 * de la cuenta no forman parte de este flujo: son gestionados
 * exclusivamente por el administrador (RF-006 / RF-030).</p>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
    }

    /**
     * Devuelve la información pública del usuario autenticado.
     *
     * @return el perfil del usuario en curso.
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        User user = currentUserService.requireAuthenticatedUser();
        return UserResponse.from(user);
    }

    /**
     * Actualiza los campos permitidos del perfil del usuario autenticado.
     * Los valores nulos indican que el campo no se debe modificar.
     *
     * @param request datos a actualizar.
     * @return el perfil actualizado.
     */
    @Transactional
    public UserResponse updateCurrentUserProfile(UpdateProfileRequest request) {
        User user = currentUserService.requireAuthenticatedUser();

        if (request.username() != null) {
            String newUsername = request.username().trim();
            if (!newUsername.equals(user.getUsername())
                    && userRepository.existsByUsername(newUsername)) {
                throw new BusinessException(HttpStatus.CONFLICT, "El nombre de usuario no está disponible.");
            }
            user.setUsername(newUsername);
        }
        if (request.firstName() != null) {
            user.setFirstName(request.firstName().trim());
        }
        if (request.lastName() != null) {
            user.setLastName(request.lastName().trim());
        }
        if (request.profilePhotoUrl() != null) {
            String url = request.profilePhotoUrl().trim();
            user.setProfilePhotoUrl(url.isEmpty() ? null : url);
        }
        if (request.showInRanking() != null) {
            user.setShowInRanking(request.showInRanking());
        }

        userRepository.save(user);
        return UserResponse.from(user);
    }

    /**
     * Cambia la contraseña del usuario autenticado tras verificar la
     * contraseña actual.
     *
     * @param request contraseña actual y nueva.
     */
    @Transactional
    public void changeCurrentUserPassword(ChangePasswordRequest request) {
        User user = currentUserService.requireAuthenticatedUser();

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La contraseña actual no es correcta.");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser diferente a la actual.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }
}
