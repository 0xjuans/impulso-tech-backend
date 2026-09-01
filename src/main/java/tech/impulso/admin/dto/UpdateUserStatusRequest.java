package tech.impulso.admin.dto;

import jakarta.validation.constraints.NotNull;
import tech.impulso.users.entity.UserStatus;

/**
 * Solicitud del administrador para activar o desactivar una cuenta
 * (RF-030).
 *
 * <p>La eliminación definitiva de cuentas no está permitida por la
 * plataforma. Para retirar el acceso a un usuario se debe utilizar el
 * estado {@link UserStatus#DESACTIVADA}, conservando su información y
 * su historial académico.</p>
 *
 * @param status nuevo estado que se aplicará a la cuenta.
 */
public record UpdateUserStatusRequest(
        @NotNull(message = "El estado es obligatorio.")
        UserStatus status
) {
}
