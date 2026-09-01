package tech.impulso.community.entity;

/**
 * Estados posibles de un reporte de contenido en la comunidad
 * (RF-034, moderación).
 */
public enum ReportStatus {

    /** El reporte fue creado por el usuario y aún no ha sido revisado. */
    PENDIENTE,

    /**
     * El administrador revisó el reporte y tomó acción (por ejemplo,
     * eliminar el contenido inapropiado).
     */
    REVISADO,

    /**
     * El administrador revisó el reporte y determinó que no procedía
     * ninguna acción.
     */
    DESESTIMADO
}
