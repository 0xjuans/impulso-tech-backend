package tech.impulso.admin.dto;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Envoltura estándar para respuestas paginadas expuestas por la API.
 *
 * <p>Se utiliza en lugar del objeto {@code Page} de Spring Data para no
 * exponer detalles internos del framework y ofrecer un contrato estable
 * para el consumidor de la API.</p>
 *
 * @param content       elementos de la página actual.
 * @param page          número de página (base 0).
 * @param size          tamaño solicitado de la página.
 * @param totalElements cantidad total de elementos que coinciden con la
 *                      consulta.
 * @param totalPages    cantidad total de páginas disponibles.
 * @param <T>           tipo de los elementos de la página.
 */
public record PagedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    /**
     * Convierte una página de Spring Data a la representación pública,
     * aplicando un mapeo elemento a elemento.
     *
     * @param page   página original.
     * @param mapper función de mapeo desde el tipo interno al DTO público.
     * @param <S>    tipo interno.
     * @param <T>    tipo público.
     * @return respuesta paginada lista para devolver desde el controlador.
     */
    public static <S, T> PagedResponse<T> from(Page<S> page, Function<S, T> mapper) {
        return new PagedResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
