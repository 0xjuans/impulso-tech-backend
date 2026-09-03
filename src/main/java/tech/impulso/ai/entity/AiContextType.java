package tech.impulso.ai.entity;

/**
 * Tipo de contexto de aprendizaje al que puede anclarse una conversación
 * con la mascota IA (RF-017).
 *
 * <p>El contexto le permite a la mascota entregar respuestas relevantes:
 * pistas sobre la lección actual, resumen del curso, ayuda con un reto,
 * etc. El servicio impide anclar conversaciones a una evaluación para no
 * comprometer respuestas protegidas (RF-028, RF-032).</p>
 */
public enum AiContextType {

    /** Conversación general sin contexto específico. */
    GENERAL,

    /** Conversación anclada a una lección concreta. */
    LESSON,

    /** Conversación anclada a un curso completo. */
    COURSE,

    /** Conversación anclada a un reto de programación. */
    CHALLENGE,

    /** Conversación anclada a un proyecto. */
    PROJECT
}
