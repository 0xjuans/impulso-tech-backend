package tech.impulso.auth.dto;

/**
 * Respuesta genérica que devuelve un mensaje informativo al cliente.
 * Se utiliza en operaciones que no requieren devolver un recurso concreto
 * (verificación de correo, solicitud de recuperación, cierre de sesión).
 *
 * @param message texto informativo para mostrar al usuario.
 */
public record MessageResponse(String message) {
}
