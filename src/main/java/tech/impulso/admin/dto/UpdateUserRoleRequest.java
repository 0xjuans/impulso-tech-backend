package tech.impulso.admin.dto;

import jakarta.validation.constraints.NotNull;
import tech.impulso.users.entity.Role;

/**
 * Solicitud del administrador para modificar el rol de un usuario
 * (RF-006 / RF-031).
 *
 * @param role nuevo rol que se asignará al usuario.
 */
public record UpdateUserRoleRequest(
        @NotNull(message = "El rol es obligatorio.")
        Role role
) {
}
