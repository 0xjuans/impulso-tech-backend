package tech.impulso.activities.entity;

/**
 * Tipos de actividad soportados por Impulso Tech en su versión inicial
 * (RF-042). Todos ellos son auto-corregibles por el sistema.
 *
 * <p>La configuración específica de cada tipo se almacena en formato JSON
 * dentro de {@link Activity#getConfig()}.</p>
 */
public enum ActivityType {

    /**
     * Pregunta con varias opciones y una única respuesta correcta.
     *
     * <p>Configuración esperada:</p>
     * <pre>{@code
     * {
     *   "question": "¿Cuál es la sintaxis correcta?",
     *   "options": ["opción A", "opción B", "opción C"],
     *   "correctIndex": 1
     * }
     * }</pre>
     *
     * <p>Respuesta esperada:</p>
     * <pre>{@code
     * { "selectedIndex": 1 }
     * }</pre>
     */
    SELECCION_MULTIPLE,

    /**
     * Pregunta cuya respuesta es verdadero o falso.
     *
     * <p>Configuración esperada:</p>
     * <pre>{@code
     * {
     *   "question": "Java soporta herencia múltiple.",
     *   "correctAnswer": false
     * }
     * }</pre>
     *
     * <p>Respuesta esperada:</p>
     * <pre>{@code
     * { "answer": false }
     * }</pre>
     */
    VERDADERO_FALSO,

    /**
     * Pregunta con respuesta corta en texto.
     *
     * <p>La comparación se realiza ignorando mayúsculas, minúsculas y
     * espacios laterales. Se admiten varias respuestas equivalentes.</p>
     *
     * <p>Configuración esperada:</p>
     * <pre>{@code
     * {
     *   "question": "¿Qué keyword define una clase en Java?",
     *   "correctAnswers": ["class"]
     * }
     * }</pre>
     *
     * <p>Respuesta esperada:</p>
     * <pre>{@code
     * { "text": "class" }
     * }</pre>
     */
    RESPUESTA_CORTA
}
