package tech.impulso.admin.dto;

import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;

import java.time.OffsetDateTime;

/**
 * Vista extendida de un usuario destinada al panel del administrador.
 *
 * <p>Incluye información de auditoría (último acceso, fechas de creación
 * y actualización) que no se expone en las respuestas dirigidas al
 * propio usuario.</p>
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
 * @param lastLoginAt     fecha del último inicio de sesión exitoso.
 * @param createdAt       fecha de creación de la cuenta.
 * @param updatedAt       fecha de la última modificación del registro.
 */
public record AdminUserResponse(
        Long id,
        String email,
        String username,
        String firstName,
        String lastName,
        String profilePhotoUrl,
        Role role,
        UserStatus status,
        OffsetDateTime emailVerifiedAt,
        OffsetDateTime lastLoginAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    /**
     * Construye la representación administrativa a partir de la entidad
     * persistente.
     *
     * @param user entidad a convertir.
     * @return DTO listo para ser devuelto por la API administrativa.
     */
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getProfilePhotoUrl(),
                user.getRole(),
                user.getStatus(),
                user.getEmailVerifiedAt(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
