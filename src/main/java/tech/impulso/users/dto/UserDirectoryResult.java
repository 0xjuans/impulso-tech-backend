package tech.impulso.users.dto;

import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

/**
 * Representación reducida de un usuario devuelta por el directorio
 * público de búsqueda que consumen módulos como Mensajería (RF-061).
 *
 * <p>Contiene únicamente los datos necesarios para reconocer al usuario
 * y decidir si iniciar una conversación con él. Deliberadamente omite
 * información sensible como el correo, el estado de la cuenta o la
 * fecha de verificación.</p>
 *
 * @param id              identificador interno del usuario.
 * @param username        nombre de usuario público.
 * @param firstName       nombre real.
 * @param lastName        apellido real.
 * @param profilePhotoUrl URL pública de la foto de perfil, si aplica.
 * @param role            rol funcional asignado.
 */
public record UserDirectoryResult(
        Long id,
        String username,
        String firstName,
        String lastName,
        String profilePhotoUrl,
        Role role
) {

    /**
     * Construye el DTO a partir de la entidad persistente.
     *
     * @param user usuario a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static UserDirectoryResult from(User user) {
        return new UserDirectoryResult(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfilePhotoUrl(),
                user.getRole()
        );
    }
}
