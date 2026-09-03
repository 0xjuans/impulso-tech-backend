package tech.impulso.search.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.search.dto.SearchResponse;
import tech.impulso.search.service.SearchService;

import java.util.Set;

/**
 * Controlador REST para la búsqueda global de la plataforma (RF-035).
 *
 * <p>Ofrece un único endpoint autenticado que devuelve, agrupados por
 * tipo, los cursos, rutas de aprendizaje, lecciones y usuarios que
 * coincidan con el término suministrado. La visibilidad de los
 * resultados depende del rol del usuario autenticado y se resuelve
 * íntegramente en el servidor.</p>
 */
@RestController
@RequestMapping("/api/search")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Búsqueda global",
        description = "Búsqueda transversal de cursos, rutas, lecciones y usuarios respetando la visibilidad por rol.")
public class SearchController {

    private final SearchService service;

    public SearchController(SearchService service) {
        this.service = service;
    }

    /**
     * Ejecuta la búsqueda global.
     *
     * @param query término a buscar (mínimo 2 caracteres).
     * @param types conjunto opcional de tipos a incluir. Valores válidos:
     *              {@code courses}, {@code routes}, {@code lessons},
     *              {@code users}. Cuando se omite, se buscan todos.
     * @param limit cantidad máxima de resultados por tipo (1 a 20, por
     *              defecto 5).
     * @return resultados agregados por tipo.
     */
    @Operation(summary = "Realizar una búsqueda global",
            description = "Devuelve hasta {limit} resultados por cada tipo solicitado. "
                    + "La visibilidad se ajusta al rol del usuario autenticado.")
    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @Parameter(description = "Término a buscar (mínimo 2 caracteres).", required = true)
            @RequestParam("q") String query,
            @Parameter(description = "Tipos a incluir separados por coma. Ej: courses,routes.")
            @RequestParam(value = "types", required = false) Set<String> types,
            @Parameter(description = "Cantidad máxima de resultados por tipo (1-20).")
            @RequestParam(value = "limit", required = false) Integer limit) {
        return ResponseEntity.ok(service.search(query, types, limit));
    }
}
