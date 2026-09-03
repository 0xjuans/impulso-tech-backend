package tech.impulso.ai.service;

import java.util.List;

/**
 * Puerta de entrada al proveedor externo de inteligencia artificial
 * (RF-017).
 *
 * <p>El servicio de la mascota siempre habla con la IA a través de esta
 * interfaz, independientemente de cuál sea el proveedor real. Esto
 * permite conectar Anthropic, OpenAI u otros sin modificar la lógica de
 * negocio, y facilita usar una implementación local durante desarrollo
 * o pruebas cuando no hay clave configurada (RF-027).</p>
 */
public interface AiProvider {

    /**
     * Genera la respuesta de la mascota a partir del historial de la
     * conversación.
     *
     * @param systemPrompt instrucciones internas y contexto de la
     *                     conversación construido íntegramente por el
     *                     backend (RF-029).
     * @param history      turnos previos ordenados cronológicamente,
     *                     excluyendo los mensajes de sistema.
     * @return respuesta generada por el proveedor.
     */
    AiCompletion complete(String systemPrompt, List<AiTurn> history);

    /**
     * Turno de una conversación, tal como se envía al proveedor.
     *
     * @param role    rol del emisor: {@code user} o {@code assistant}.
     * @param content contenido literal.
     */
    record AiTurn(String role, String content) {
    }

    /**
     * Respuesta del proveedor.
     *
     * @param content    contenido generado por la IA.
     * @param tokensUsed cantidad de tokens consumidos, o {@code null}
     *                   cuando el proveedor no la reporta.
     */
    record AiCompletion(String content, Integer tokensUsed) {
    }
}
