package tech.impulso.gamification.dto;

import tech.impulso.gamification.entity.RankingPeriod;

import java.util.List;

/**
 * Respuesta agregada del ranking, con la lista de las primeras
 * posiciones y la ubicación del propio usuario cuando exista.
 *
 * @param period  periodo utilizado para calcular el ranking.
 * @param entries primeras entradas ordenadas por posición.
 * @param me      entrada correspondiente al usuario autenticado
 *                (puede quedar fuera del top mostrado); es {@code null}
 *                cuando el usuario aún no cumple los criterios.
 */
public record RankingResponse(
        RankingPeriod period,
        List<RankingEntryResponse> entries,
        RankingEntryResponse me
) {
}
