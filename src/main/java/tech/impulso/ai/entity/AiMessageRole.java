package tech.impulso.ai.entity;

/**
 * Rol del emisor de un mensaje en una conversación con la mascota IA
 * (RF-017).
 *
 * <p>Se mantiene un rol {@link #SYSTEM} independiente del usuario para
 * dejar constancia del mensaje inicial que fija las reglas y el contexto
 * de la conversación. Esto separa las instrucciones internas de las
 * entradas del estudiante y facilita auditar cualquier intento de
 * manipulación (RF-029).</p>
 */
public enum AiMessageRole {

    /** Instrucciones internas de la plataforma; no se muestran al usuario. */
    SYSTEM,

    /** Mensajes redactados por el estudiante. */
    USER,

    /** Respuestas generadas por la mascota IA. */
    ASSISTANT
}
