package tech.impulso.files.entity;

/**
 * Propósito funcional de un archivo almacenado (RF-059).
 *
 * <p>El propósito determina las validaciones aplicables (tamaño máximo,
 * tipos MIME permitidos) y la visibilidad por defecto.</p>
 */
public enum FilePurpose {

    /** Foto de perfil de un usuario. */
    PROFILE_PHOTO,

    /** Imagen de portada de un curso o ruta de aprendizaje. */
    COURSE_COVER,

    /** Recurso educativo publicado en la biblioteca. */
    RESOURCE,

    /** Archivo adjunto a una entrega (proyecto o laboratorio). */
    SUBMISSION,

    /** Adjunto a un ticket de soporte. */
    SUPPORT_ATTACHMENT,

    /** Adjunto genérico dentro de una conversación. */
    GENERAL
}
