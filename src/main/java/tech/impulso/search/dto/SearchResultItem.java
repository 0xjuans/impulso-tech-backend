package tech.impulso.search.dto;

/**
 * Representa un resultado individual devuelto por la búsqueda global
 * (RF-035).
 *
 * <p>Los campos se generalizan para todos los tipos de entidad
 * (cursos, rutas, lecciones, usuarios). Los valores no aplicables para
 * un tipo se envían como {@code null}.</p>
 *
 * @param id        identificador de la entidad de origen.
 * @param type      tipo lógico del resultado ({@code COURSE}, {@code ROUTE},
 *                  {@code LESSON}, {@code USER}).
 * @param title     título o nombre visible del resultado.
 * @param subtitle  descripción breve u otra información secundaria.
 * @param imageUrl  URL de la imagen o avatar asociado, si existe.
 * @param status    estado del contenido (o del usuario), cuando aplica.
 * @param parentId  identificador del elemento padre (por ejemplo, el
 *                  identificador del curso al que pertenece la lección).
 */
public record SearchResultItem(
        Long id,
        String type,
        String title,
        String subtitle,
        String imageUrl,
        String status,
        Long parentId
) {
}
