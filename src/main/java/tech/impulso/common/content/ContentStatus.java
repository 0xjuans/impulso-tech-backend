package tech.impulso.common.content;

/**
 * Estados del ciclo de vida de un contenido educativo (rutas, cursos,
 * módulos, lecciones, actividades, etc.).
 *
 * <p>El estado determina la visibilidad del contenido para el estudiante
 * y la posibilidad de modificarlo por parte del instructor.</p>
 */
public enum ContentStatus {

    /** Contenido en construcción; no está disponible para los estudiantes. */
    BORRADOR,

    /** Contenido publicado y disponible para los estudiantes autorizados. */
    PUBLICADO,

    /**
     * Contenido deshabilitado. No aparece en nuevas búsquedas ni permite
     * nuevas inscripciones, pero se conserva la información y el progreso
     * de los estudiantes que ya lo habían iniciado.
     */
    DESHABILITADO
}
