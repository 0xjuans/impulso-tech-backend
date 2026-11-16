package tech.impulso.users.service;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tech.impulso.auth.dto.UserResponse;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.users.dto.ChangePasswordRequest;
import tech.impulso.users.dto.UpdateProfileRequest;
import tech.impulso.users.dto.UpdateSignatureRequest;
import tech.impulso.users.dto.UserDirectoryResult;
import tech.impulso.users.entity.User;
import tech.impulso.users.repository.UserRepository;

import java.util.List;

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

    /**
     * Guarda la firma del instructor autenticado a partir del data URL
     * enviado por el frontend. La validación de formato y tamaño la
     * realiza la propia {@link UpdateSignatureRequest}, aquí sólo
     * persistimos el valor limpio.
     *
     * @param request payload con el data URL de la firma.
     * @return perfil actualizado del usuario.
     */
    @Transactional
    public UserResponse updateSignature(UpdateSignatureRequest request) {
        User user = currentUserService.requireAuthenticatedUser();
        user.setSignatureImageUrl(request.signatureImage().trim());
        userRepository.save(user);
        return UserResponse.from(user);
    }

    /**
     * Elimina la firma cargada por el instructor autenticado. Los
     * certificados posteriores se emitirán con la línea vacía sobre el
     * nombre del instructor.
     */
    @Transactional
    public UserResponse deleteSignature() {
        User user = currentUserService.requireAuthenticatedUser();
        user.setSignatureImageUrl(null);
        userRepository.save(user);
        return UserResponse.from(user);
    }

    /** Tope superior de resultados devueltos por el directorio. */
    private static final int MAX_DIRECTORY_RESULTS = 10;

    /** Longitud mínima aceptada del término de búsqueda para evitar cargas innecesarias. */
    private static final int MIN_QUERY_LENGTH = 2;

    /**
     * Busca usuarios activos por nombre de usuario, nombre o apellido
     * para poblar el directorio consumido desde la mensajería directa
     * (RF-061). El usuario que hace la búsqueda queda excluido de los
     * resultados y no se devuelven cuentas inactivas.
     *
     * @param query término a buscar; si está vacío o es más corto que
     *              el mínimo se devuelve una lista vacía.
     * @param limit cantidad máxima de resultados solicitada.
     * @return coincidencias listas para renderizar.
     */
    @Transactional(readOnly = true)
    public List<UserDirectoryResult> searchDirectory(String query, int limit) {
        if (query == null) return List.of();
        String trimmed = query.trim();
        if (trimmed.length() < MIN_QUERY_LENGTH) return List.of();

        User me = currentUserService.requireAuthenticatedUser();
        int bounded = Math.max(1, Math.min(limit, MAX_DIRECTORY_RESULTS));
        Pageable pageable = PageRequest.of(0, bounded);
        String term = "%" + trimmed.toLowerCase() + "%";

        return userRepository.searchDirectory(term, me.getId(), pageable)
                .stream()
                .map(UserDirectoryResult::from)
                .toList();
    }
}
