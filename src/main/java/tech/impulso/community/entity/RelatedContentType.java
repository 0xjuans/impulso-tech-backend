package tech.impulso.community.entity;

/**
 * Tipos de contenido educativo con los que una publicación de la
 * comunidad puede vincularse (RF-034).
 *
 * <p>Permite abrir un hilo desde el propio contenido y facilita al
 * instructor identificar sobre qué está preguntando el estudiante.</p>
 */
public enum RelatedContentType {

    LEARNING_ROUTE,
    COURSE,
    MODULE,
    LESSON,
    ACTIVITY,
    CHALLENGE,
    LAB,
    EVALUATION,
    PROJECT
}
