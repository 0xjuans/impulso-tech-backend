package tech.impulso.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base para los errores controlados de negocio.
 *
 * <p>Las excepciones que extienden esta clase representan situaciones
 * previstas por las reglas de negocio (por ejemplo: un correo ya
 * registrado, un token expirado, credenciales inválidas). El
 * {@link GlobalExceptionHandler} las convierte en una respuesta HTTP con
 * el {@link HttpStatus} configurado y un mensaje orientado al cliente.</p>
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    /**
     * Crea una nueva excepción de negocio.
     *
     * @param status  código HTTP con el que se debe responder al cliente.
     * @param message mensaje orientado al cliente.
     */
    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
