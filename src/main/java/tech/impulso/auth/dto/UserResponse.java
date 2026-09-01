package tech.impulso.auth.dto;

import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;

import java.time.OffsetDateTime;

/**
 * Representación pública de un usuario devuelta por la API.
 *
 * <p>Contiene únicamente la información necesaria para que el cliente
 * pueda mostrar el perfil y tomar decisiones de navegación. Nunca se
 * exponen datos sensibles como el hash de la contraseña o tokens
 * internos.</p>
 *
 * @param id              identificador interno del usuario.
 * @param email           correo electrónico registrado.
 * @param username        nombre de usuario público.
 * @param firstName       nombre real.
 * @param lastName        apellido real.
 * @param profilePhotoUrl URL pública de la foto de perfil, si aplica.
 * @param role            rol funcional asignado.
 * @param status          estado del ciclo de vida de la cuenta.
 * @param emailVerifiedAt fecha en que se verificó el correo, si aplica.
 * @param createdAt       fecha de creación de la cuenta.
 */
public record UserResponse(
        Long id,
        String email,
        String username,
        String firstName,
        String lastName,
        String profilePhotoUrl,
        Role role,
        UserStatus status,
        OffsetDateTime emailVerifiedAt,
        OffsetDateTime createdAt
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente, evitando exponer campos internos.
     *
     * @param user entidad a convertir.
     * @return DTO listo para ser devuelto por la API.
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfilePhotoUrl(),
                user.getRole(),
                user.getStatus(),
                user.getEmailVerifiedAt(),
                user.getCreatedAt()
        );
    }
}
