package tech.impulso.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.users.entity.User;

import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad {@link User}.
 *
 * <p>Expone las operaciones de persistencia necesarias para el módulo de
 * autenticación y gestión de usuarios. Al extender {@link JpaRepository}
 * se obtienen las operaciones CRUD estándar.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su correo electrónico. Se utiliza durante el
     * inicio de sesión y los procesos de recuperación de contraseña.
     *
     * @param email correo electrónico a consultar.
     * @return el usuario asociado al correo si existe.
     */
    Optional<User> findByEmail(String email);

    /**
     * Busca un usuario por su nombre de usuario público.
     *
     * @param username nombre de usuario a consultar.
     * @return el usuario asociado al nombre si existe.
     */
    Optional<User> findByUsername(String username);

    /**
     * Indica si existe un usuario registrado con el correo suministrado.
     *
     * @param email correo electrónico a validar.
     * @return {@code true} cuando el correo ya está en uso.
     */
    boolean existsByEmail(String email);

    /**
     * Indica si existe un usuario registrado con el nombre de usuario
     * suministrado.
     *
     * @param username nombre de usuario a validar.
     * @return {@code true} cuando el nombre de usuario ya está en uso.
     */
    boolean existsByUsername(String username);
}
