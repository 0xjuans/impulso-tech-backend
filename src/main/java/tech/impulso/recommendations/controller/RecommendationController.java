package tech.impulso.recommendations.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.recommendations.dto.RecommendationsResponse;
import tech.impulso.recommendations.service.RecommendationService;

/**
 * Controlador REST del motor de recomendaciones personalizadas
 * (RF-053).
 *
 * <p>Expone un único endpoint autenticado que devuelve sugerencias de
 * cursos, rutas, retos y recursos afines al perfil del estudiante
 * autenticado.</p>
 */
@RestController
@RequestMapping("/api/recommendations")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recomendaciones",
        description = "Sugerencias personalizadas de contenidos basadas en el historial del estudiante.")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @Operation(summary = "Obtener mis recomendaciones",
            description = "Devuelve cursos, rutas, retos y recursos afines al perfil del estudiante autenticado.")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    @GetMapping
    public ResponseEntity<RecommendationsResponse> myRecommendations() {
        return ResponseEntity.ok(service.recommend());
    }
}
