package tech.impulso.ai.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementación local de {@link AiProvider} utilizada mientras no haya
 * un proveedor externo configurado (RF-017, RF-027).
 *
 * <p>Genera una respuesta canónica que reconoce el último mensaje del
 * estudiante y le indica de forma explícita que la mascota está en modo
 * de desarrollo. De esta manera el flujo completo puede probarse sin
 * depender de credenciales externas ni consumir cuotas de API.</p>
 *
 * <p>Se registra únicamente cuando no existe otro bean de
 * {@link AiProvider} en el contexto; al configurar un proveedor real
 * este stub queda deshabilitado automáticamente.</p>
 */
@Component
@ConditionalOnMissingBean(value = AiProvider.class, ignored = StubAiProvider.class)
public class StubAiProvider implements AiProvider {

    @Override
    public AiCompletion complete(String systemPrompt, List<AiTurn> history) {
        String lastUser = history.stream()
                .filter(turn -> "user".equalsIgnoreCase(turn.role()))
                .reduce((first, second) -> second)
                .map(AiTurn::content)
                .orElse("");

        String answer = """
                (Modo de desarrollo) Recibí tu mensaje: "%s".

                La mascota IA todavía no está conectada a un proveedor externo. \
                En cuanto se configuren las credenciales podré ayudarte con \
                explicaciones, pistas y ejemplos personalizados según tu contexto \
                de aprendizaje.
                """.formatted(lastUser.strip());

        return new AiCompletion(answer, null);
    }
}
