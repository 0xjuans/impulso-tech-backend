package tech.impulso.activities.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tech.impulso.activities.entity.Activity;
import tech.impulso.activities.entity.ActivityType;
import tech.impulso.common.exception.BusinessException;

import java.util.ArrayList;
import java.util.List;

/**
 * Evaluador de respuestas de actividades auto-corregibles (RF-042).
 *
 * <p>Cada tipo de actividad conoce la estructura de su propia
 * configuración y de la respuesta esperada. El evaluador delega en el
 * método adecuado y devuelve {@code true} cuando la respuesta es
 * correcta.</p>
 *
 * <p>Este componente también expone utilidades para preparar la
 * configuración antes de exponerla al estudiante, eliminando los campos
 * que revelarían la solución.</p>
 */
@Component
public class ActivityGrader {

    private final ObjectMapper objectMapper;

    public ActivityGrader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Valida que una cadena JSON de configuración cumpla la estructura
     * esperada por el tipo indicado.
     *
     * @param type   tipo de la actividad.
     * @param config cadena JSON con la configuración.
     */
    public void validateConfig(ActivityType type, String config) {
        JsonNode node = parse(config, "La configuración de la actividad no es un JSON válido.");
        switch (type) {
            case SELECCION_MULTIPLE -> validateMultipleChoiceConfig(node);
            case VERDADERO_FALSO    -> validateTrueFalseConfig(node);
            case RESPUESTA_CORTA    -> validateShortAnswerConfig(node);
        }
    }

    /**
     * Devuelve una copia de la configuración lista para ser expuesta a
     * un estudiante, eliminando los campos que revelan la solución.
     *
     * @param type   tipo de la actividad.
     * @param config configuración interna completa.
     * @return configuración segura para el estudiante.
     */
    public String sanitizeConfigForStudent(ActivityType type, String config) {
        JsonNode node = parse(config, "La configuración almacenada no es un JSON válido.");
        if (!(node instanceof ObjectNode object)) {
            return config;
        }
        ObjectNode copy = object.deepCopy();
        switch (type) {
            case SELECCION_MULTIPLE -> copy.remove("correctIndex");
            case VERDADERO_FALSO    -> copy.remove("correctAnswer");
            case RESPUESTA_CORTA    -> copy.remove("correctAnswers");
        }
        try {
            return objectMapper.writeValueAsString(copy);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "No fue posible preparar la configuración de la actividad.");
        }
    }

    /**
     * Evalúa la respuesta del estudiante contra la configuración de la
     * actividad.
     *
     * @param activity actividad sobre la que se responde.
     * @param answer   respuesta del estudiante en formato JSON.
     * @return {@code true} si la respuesta es correcta.
     */
    public boolean isCorrect(Activity activity, String answer) {
        JsonNode config = parse(activity.getConfig(), "La configuración almacenada no es un JSON válido.");
        JsonNode response = parse(answer, "La respuesta enviada no es un JSON válido.");
        return switch (activity.getType()) {
            case SELECCION_MULTIPLE -> checkMultipleChoice(config, response);
            case VERDADERO_FALSO    -> checkTrueFalse(config, response);
            case RESPUESTA_CORTA    -> checkShortAnswer(config, response);
        };
    }

    private boolean checkMultipleChoice(JsonNode config, JsonNode response) {
        if (!config.has("correctIndex") || !response.has("selectedIndex")) {
            return false;
        }
        return config.get("correctIndex").asInt(-1) == response.get("selectedIndex").asInt(-2);
    }

    private boolean checkTrueFalse(JsonNode config, JsonNode response) {
        if (!config.has("correctAnswer") || !response.has("answer")) {
            return false;
        }
        return config.get("correctAnswer").asBoolean() == response.get("answer").asBoolean(!config.get("correctAnswer").asBoolean());
    }

    private boolean checkShortAnswer(JsonNode config, JsonNode response) {
        if (!config.has("correctAnswers") || !response.has("text")) {
            return false;
        }
        String submitted = normalize(response.get("text").asText(""));
        List<String> valid = new ArrayList<>();
        config.get("correctAnswers").forEach(node -> valid.add(normalize(node.asText(""))));
        return valid.stream().anyMatch(candidate -> candidate.equals(submitted));
    }

    private void validateMultipleChoiceConfig(JsonNode node) {
        if (!node.has("options") || !node.get("options").isArray() || node.get("options").isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "La configuración de selección múltiple debe incluir un arreglo 'options' con al menos una opción.");
        }
        if (!node.has("correctIndex") || !node.get("correctIndex").isInt()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "La configuración de selección múltiple debe indicar 'correctIndex' como entero.");
        }
        int index = node.get("correctIndex").asInt();
        int size = node.get("options").size();
        if (index < 0 || index >= size) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "El campo 'correctIndex' debe apuntar a una opción existente.");
        }
    }

    private void validateTrueFalseConfig(JsonNode node) {
        if (!node.has("correctAnswer") || !node.get("correctAnswer").isBoolean()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "La configuración de verdadero/falso debe indicar 'correctAnswer' como booleano.");
        }
    }

    private void validateShortAnswerConfig(JsonNode node) {
        if (!node.has("correctAnswers") || !node.get("correctAnswers").isArray() || node.get("correctAnswers").isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "La configuración de respuesta corta debe incluir 'correctAnswers' con al menos un valor.");
        }
    }

    private JsonNode parse(String json, String errorMessage) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception ex) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, errorMessage);
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
