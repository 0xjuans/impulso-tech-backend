package tech.impulso.users.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;

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

    /**
     * Indica si existe al menos un usuario con el rol suministrado.
     * Se utiliza durante el arranque para determinar si es necesario
     * crear el primer administrador.
     *
     * @param role rol a verificar.
     * @return {@code true} cuando existe al menos un usuario con ese rol.
     */
    boolean existsByRole(Role role);

    /**
     * Búsqueda paginada de usuarios para el panel del administrador. Los
     * parámetros son opcionales: cuando llegan como {@code null} no se
     * aplica el filtro correspondiente.
     *
     * @param search   fragmento a buscar en el correo, el nombre de usuario,
     *                 el nombre o el apellido (búsqueda insensible a
     *                 mayúsculas).
     * @param role     rol al que restringir los resultados.
     * @param status   estado al que restringir los resultados.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con los usuarios que coinciden con los filtros.
     */
    @Query("""
            select u from User u
            where (:search is null or
                   lower(u.email)     like lower(concat('%', :search, '%')) or
                   lower(u.username)  like lower(concat('%', :search, '%')) or
                   lower(u.firstName) like lower(concat('%', :search, '%')) or
                   lower(u.lastName)  like lower(concat('%', :search, '%')))
              and (:role   is null or u.role   = :role)
              and (:status is null or u.status = :status)
            """)
    Page<User> search(@Param("search") String search,
                      @Param("role") Role role,
                      @Param("status") UserStatus status,
                      Pageable pageable);
}
