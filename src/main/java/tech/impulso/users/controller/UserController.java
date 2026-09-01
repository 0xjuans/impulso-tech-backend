package tech.impulso.users.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.auth.dto.MessageResponse;
import tech.impulso.auth.dto.UserResponse;
import tech.impulso.users.dto.ChangePasswordRequest;
import tech.impulso.users.dto.UpdateProfileRequest;
import tech.impulso.users.service.UserService;

/**
 * Controlador REST para las operaciones que el usuario autenticado puede
 * realizar sobre su propio perfil (RF-005).
 */
@RestController
@RequestMapping("/api/users/me")
@Tag(name = "Perfil del usuario", description = "Operaciones sobre el perfil del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Devuelve la información pública del usuario autenticado.
     *
     * @return perfil del usuario en curso.
     */
    @Operation(summary = "Consultar el perfil propio",
            description = "Devuelve la información pública del usuario autenticado.")
    @GetMapping
    public ResponseEntity<UserResponse> getProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    /**
     * Actualiza los campos del perfil permitidos por la plataforma.
     *
     * @param request datos a actualizar.
     * @return perfil actualizado.
     */
    @Operation(summary = "Actualizar el perfil propio",
            description = "Modifica el nombre de usuario, el nombre real, el apellido o la foto de perfil.")
    @PatchMapping
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateCurrentUserProfile(request));
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     *
     * @param request contraseña actual y nueva contraseña.
     * @return mensaje de confirmación.
     */
    @Operation(summary = "Cambiar la contraseña propia",
            description = "Requiere la contraseña actual para autorizar el cambio.")
    @PostMapping("/password")
    public ResponseEntity<MessageResponse> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userService.changeCurrentUserPassword(request);
        return ResponseEntity.ok(new MessageResponse("La contraseña ha sido actualizada correctamente."));
    }
}
