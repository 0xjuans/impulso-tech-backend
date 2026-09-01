package tech.impulso.community.entity;

/**
 * Tipos de contenido de la comunidad que pueden ser reportados
 * (RF-034, moderación).
 */
public enum ReportTargetType {

    /** El contenido reportado es una publicación. */
    POST,

    /** El contenido reportado es una respuesta a una publicación. */
    REPLY
}
