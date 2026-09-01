package tech.impulso.gamification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.gamification.dto.RankingResponse;
import tech.impulso.gamification.entity.RankingPeriod;
import tech.impulso.gamification.service.RankingService;

/**
 * Controlador REST del ranking de estudiantes (RF-021 / RF-049).
 */
@RestController
@RequestMapping("/api/rankings")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ranking de estudiantes",
        description = "Consulta de las clasificaciones globales por experiencia acumulada.")
public class RankingController {

    private final RankingService service;

    public RankingController(RankingService service) {
        this.service = service;
    }

    /**
     * Devuelve el ranking global correspondiente al periodo indicado.
     *
     * @param period periodo utilizado para agrupar la XP.
     * @param limit  cantidad máxima de entradas a devolver.
     * @return ranking con las primeras posiciones y la ubicación del
     *         usuario autenticado cuando aplique.
     */
    @Operation(summary = "Consultar el ranking global",
            description = "Devuelve la clasificación por XP para el periodo indicado, junto con la posición del usuario autenticado.")
    @GetMapping
    public ResponseEntity<RankingResponse> getGlobalRanking(
            @RequestParam(value = "period", defaultValue = "ALL_TIME") RankingPeriod period,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {
        return ResponseEntity.ok(service.getRanking(period, limit));
    }
}
