package tech.impulso.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Manejador global de excepciones de la API.
 *
 * <p>Centraliza la conversión de cualquier excepción no controlada en una
 * respuesta {@link ApiError} consistente. Su objetivo es evitar exponer
 * información sensible al cliente (trazas, mensajes técnicos, detalles de
 * la base de datos) y ofrecer un contrato de error uniforme para el
 * consumidor de la API.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja los errores de validación producidos por Bean Validation en
     * los DTOs anotados con {@code @Valid}.
     *
     * @param ex      excepción de validación con el detalle de los campos.
     * @param request petición HTTP en la que se produjo el error.
     * @return respuesta con código 400 y el listado de campos inválidos.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> "%s: %s".formatted(fieldError.getField(), fieldError.getDefaultMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Los datos suministrados no son válidos.", request, details);
    }

    /**
     * Maneja las violaciones de restricciones detectadas fuera del ciclo
     * habitual de validación de DTOs (por ejemplo, parámetros de consulta).
     *
     * @param ex      excepción con las restricciones violadas.
     * @param request petición HTTP asociada.
     * @return respuesta con código 400 y el detalle de las violaciones.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(violation -> "%s: %s".formatted(violation.getPropertyPath(), violation.getMessage()))
                .toList();
        return build(HttpStatus.BAD_REQUEST, "Los datos suministrados no son válidos.", request, details);
    }

    /**
     * Maneja los intentos de autenticación fallidos.
     *
     * @param ex      excepción de autenticación producida por Spring Security.
     * @param request petición HTTP asociada.
     * @return respuesta con código 401.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "No fue posible autenticar la solicitud.", request, null);
    }

    /**
     * Maneja los intentos de acceso a recursos para los que el usuario no
     * cuenta con permisos suficientes.
     *
     * @param ex      excepción de acceso denegado.
     * @param request petición HTTP asociada.
     * @return respuesta con código 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "No cuenta con permisos para acceder a este recurso.", request, null);
    }

    /**
     * Maneja las excepciones de negocio controladas por la aplicación.
     * Utiliza el código HTTP definido en la propia excepción para
     * mantener la coherencia entre la regla violada y la respuesta.
     *
     * @param ex      excepción de negocio.
     * @param request petición HTTP asociada.
     * @return respuesta con el código y mensaje configurados en la excepción.
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> handleBusiness(BusinessException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getMessage(), request, null);
    }

    /**
     * Manejador de último recurso para cualquier excepción no controlada.
     * Registra la excepción en el log del servidor y devuelve un mensaje
     * genérico al cliente.
     *
     * @param ex      excepción capturada.
     * @param request petición HTTP asociada.
     * @return respuesta con código 500 y mensaje genérico.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado al procesar {} {}", request.getMethod(), request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado al procesar la solicitud.", request, null);
    }

    /**
     * Construye la respuesta {@link ApiError} con la información común de
     * error.
     *
     * @param status  código HTTP a devolver.
     * @param message mensaje orientado al cliente.
     * @param request petición HTTP asociada, utilizada para obtener la ruta.
     * @param details detalles adicionales opcionales.
     * @return respuesta HTTP con el cuerpo del error.
     */
    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request, List<String> details) {
        ApiError body = new ApiError(
                OffsetDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI(),
                details
        );
        return ResponseEntity.status(status).body(body);
    }
}
