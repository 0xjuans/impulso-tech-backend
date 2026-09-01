package tech.impulso.resources.entity;

/**
 * Tipos funcionales de recursos educativos soportados por la biblioteca
 * de Impulso Tech (RF-024).
 */
public enum ResourceType {

    /** Documento en formato PDF. */
    PDF,

    /** Video (alojado externamente o cargado a la plataforma). */
    VIDEO,

    /** Imagen o infografía. */
    IMAGEN,

    /** Fragmento de código o repositorio. */
    CODIGO,

    /** Enlace externo a un recurso disponible en otra plataforma. */
    ENLACE_EXTERNO,

    /** Documento en otro formato editable (Word, Google Docs, Markdown). */
    DOCUMENTO,

    /** Guía o tutorial autoexplicativo. */
    GUIA
}
