package tech.impulso.users.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tech.impulso.common.ratelimit.RateLimit;
import tech.impulso.users.dto.UserDirectoryResult;
import tech.impulso.users.service.UserService;

/**
 * Controlador REST del directorio público de usuarios (RF-061).
 *
 * <p>Sirve como fuente de datos para el buscador de destinatarios de
 * la mensajería directa. Devuelve únicamente información pública
 * necesaria para reconocer al usuario (nombre, foto, rol), nunca su
 * correo ni el estado interno de la cuenta.</p>
 */
@RestController
@RequestMapping("/api/users/directory")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Directorio de usuarios",
        description = "Búsqueda pública de usuarios para iniciar conversaciones.")
public class UserDirectoryController {

    private final UserService userService;

    public UserDirectoryController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Busca usuarios cuyo nombre de usuario, nombre o apellido contengan
     * el término indicado. El usuario autenticado nunca aparece en sus
     * propios resultados.
     */
    @Operation(summary = "Buscar en el directorio",
            description = "Búsqueda por prefijo o subcadena en username, firstName y lastName. "
                    + "Devuelve al máximo 10 resultados activos, sin datos sensibles.")
    @RateLimit(bucket = "user-directory-search", limit = 30, windowSeconds = 60)
    @GetMapping("/search")
    public ResponseEntity<List<UserDirectoryResult>> search(
            @RequestParam("q") String query,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(userService.searchDirectory(query, limit));
    }
}
