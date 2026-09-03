package tech.impulso.recommendations.dto;

import java.util.List;

/**
 * Respuesta agregada del motor de recomendaciones (RF-053).
 *
 * @param profileSummary  descripción breve del perfil calculado a
 *                        partir del historial del estudiante.
 * @param dominantLevel   nivel de dificultad dominante detectado.
 * @param technologies    tecnologías consideradas para la afinidad.
 * @param courses         cursos recomendados.
 * @param routes          rutas recomendadas.
 * @param challenges      retos recomendados.
 * @param resources       recursos educativos recomendados.
 */
public record RecommendationsResponse(
        String profileSummary,
        String dominantLevel,
        List<String> technologies,
        List<Recommendation> courses,
        List<Recommendation> routes,
        List<Recommendation> challenges,
        List<Recommendation> resources
) {
}
