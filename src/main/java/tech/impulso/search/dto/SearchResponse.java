package tech.impulso.search.dto;

import java.util.List;

/**
 * Respuesta agregada de la búsqueda global (RF-035).
 *
 * <p>Cada campo agrupa los resultados por tipo de entidad. Los tipos no
 * solicitados por el cliente se devuelven como listas vacías, no como
 * {@code null}, para simplificar el consumo desde el frontend.</p>
 *
 * @param query    término de búsqueda aplicado, ya normalizado.
 * @param courses  cursos coincidentes visibles para el solicitante.
 * @param routes   rutas de aprendizaje coincidentes.
 * @param lessons  lecciones coincidentes.
 * @param users    usuarios coincidentes.
 */
public record SearchResponse(
        String query,
        List<SearchResultItem> courses,
        List<SearchResultItem> routes,
        List<SearchResultItem> lessons,
        List<SearchResultItem> users
) {
}
