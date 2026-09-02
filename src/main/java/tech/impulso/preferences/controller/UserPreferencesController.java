package tech.impulso.preferences.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.preferences.dto.UpdatePreferencesRequest;
import tech.impulso.preferences.dto.UserPreferencesResponse;
import tech.impulso.preferences.service.UserPreferencesService;

/**
 * Controlador REST para las preferencias del usuario autenticado
 * (RF-060).
 */
@RestController
@RequestMapping("/api/users/me/preferences")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Preferencias del usuario",
        description = "Configuración de notificaciones, privacidad e idioma preferido del usuario.")
public class UserPreferencesController {

    private final UserPreferencesService service;

    public UserPreferencesController(UserPreferencesService service) {
        this.service = service;
    }

    @Operation(summary = "Consultar mis preferencias")
    @GetMapping
    public ResponseEntity<UserPreferencesResponse> getMyPreferences() {
        return ResponseEntity.ok(service.getMyPreferences());
    }

    @Operation(summary = "Actualizar mis preferencias")
    @PatchMapping
    public ResponseEntity<UserPreferencesResponse> updateMyPreferences(
            @Valid @RequestBody UpdatePreferencesRequest request) {
        return ResponseEntity.ok(service.updateMyPreferences(request));
    }
}
