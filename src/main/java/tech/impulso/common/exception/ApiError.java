package tech.impulso.common.exception;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Representación estándar de un error devuelto por la API.
 *
 * <p>Todos los errores expuestos por la plataforma utilizan esta
 * estructura para mantener respuestas consistentes y evitar filtrar
 * información sensible del sistema (trazas, SQL, detalles internos).</p>
 *
 * @param timestamp momento en el que se generó la respuesta de error.
 * @param status    código HTTP asociado al error.
 * @param error     descripción corta del código HTTP.
 * @param message   mensaje de negocio orientado al cliente.
 * @param path      ruta de la petición que produjo el error.
 * @param details   detalles adicionales, típicamente errores de validación
 *                  por campo. Puede ser {@code null} cuando no aplique.
 */
public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
}
