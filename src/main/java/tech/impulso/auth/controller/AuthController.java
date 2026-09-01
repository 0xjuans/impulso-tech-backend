package tech.impulso.auth.controller;

import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.impulso.auth.dto.AuthResponse;
import tech.impulso.auth.dto.ForgotPasswordRequest;
import tech.impulso.auth.dto.LoginRequest;
import tech.impulso.auth.dto.MessageResponse;
import tech.impulso.auth.dto.RegisterRequest;
import tech.impulso.auth.dto.ResetPasswordRequest;
import tech.impulso.auth.dto.UserResponse;
import tech.impulso.auth.service.AuthService;

/**
 * Controlador REST que expone las operaciones de autenticación de
 * Impulso Tech.
 *
 * <p>Cubre los flujos definidos en los requerimientos RF-001, RF-002,
 * RF-003, RF-007, RF-054 y RF-055. Delega toda la lógica en
 * {@link AuthService} y se limita a mapear las peticiones HTTP a los
 * casos de uso correspondientes.</p>
 */
@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "Autenticación", description = "Operaciones de registro, verificación e inicio de sesión.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Registra un nuevo usuario en la plataforma y le envía el correo de
     * verificación.
     *
     * @param request datos de registro suministrados por el cliente.
     * @return información pública del usuario creado.
     */
    @Operation(summary = "Registrar un nuevo usuario",
            description = "Crea la cuenta en estado PENDIENTE_VERIFICACION y envía un enlace de verificación al correo indicado.")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Confirma la propiedad del correo electrónico mediante el token
     * enviado en el enlace de verificación.
     *
     * @param token valor recibido en el parámetro del enlace.
     * @return mensaje de confirmación.
     */
    @Operation(summary = "Verificar el correo electrónico",
            description = "Activa la cuenta asociada al token de verificación enviado por correo.")
    @PostMapping("/verify")
    public ResponseEntity<MessageResponse> verify(@RequestParam("token") @NotBlank String token) {
        authService.verifyEmail(token);
        return ResponseEntity.ok(new MessageResponse("La cuenta ha sido verificada correctamente."));
    }

    /**
     * Reenvía el correo de verificación al usuario que aún no confirma
     * su cuenta.
     *
     * @param request solicitud que contiene el correo del usuario.
     * @return mensaje genérico de confirmación.
     */
    @Operation(summary = "Reenviar el correo de verificación",
            description = "Genera un nuevo enlace de verificación cuando el usuario no ha confirmado su cuenta.")
    @PostMapping("/verify/resend")
    public ResponseEntity<MessageResponse> resendVerification(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.resendVerification(request.email());
        return ResponseEntity.ok(new MessageResponse("Si el correo corresponde a una cuenta pendiente, se ha reenviado el enlace de verificación."));
    }

    /**
     * Autentica al usuario y devuelve un token JWT para las peticiones
     * posteriores.
     *
     * @param request credenciales del usuario.
     * @return token de acceso y datos del usuario autenticado.
     */
    @Operation(summary = "Iniciar sesión",
            description = "Valida las credenciales y emite un token JWT para las peticiones posteriores.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Invalida el token de acceso actual del usuario.
     *
     * @param request petición HTTP en curso, que contiene los claims
     *                inyectados por {@code JwtAuthenticationFilter}.
     * @return mensaje de confirmación.
     */
    @Operation(summary = "Cerrar sesión",
            description = "Invalida el token JWT actual mediante la lista negra en Redis.")
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("jwtClaims");
        if (claims != null) {
            authService.logout(claims);
        }
        return ResponseEntity.ok(new MessageResponse("La sesión ha sido cerrada correctamente."));
    }

    /**
     * Inicia el flujo de recuperación de contraseña.
     *
     * @param request solicitud con el correo asociado a la cuenta.
     * @return mensaje genérico de confirmación.
     */
    @Operation(summary = "Solicitar recuperación de contraseña",
            description = "Envía un enlace temporal al correo del usuario para restablecer su contraseña. Responde siempre con éxito para no revelar la existencia de cuentas.")
    @PostMapping("/password/forgot")
    public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request);
        return ResponseEntity.ok(new MessageResponse("Si el correo corresponde a una cuenta registrada, recibirá un enlace de recuperación."));
    }

    /**
     * Aplica la nueva contraseña utilizando el token recibido por correo.
     *
     * @param request solicitud con el token y la nueva contraseña.
     * @return mensaje de confirmación.
     */
    @Operation(summary = "Restablecer contraseña",
            description = "Actualiza la contraseña del usuario a partir de un token de recuperación válido.")
    @PostMapping("/password/reset")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponse("La contraseña ha sido actualizada correctamente."));
    }
}
