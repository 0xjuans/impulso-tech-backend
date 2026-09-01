package tech.impulso.auth.service;

import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.auth.dto.AuthResponse;
import tech.impulso.auth.dto.ForgotPasswordRequest;
import tech.impulso.auth.dto.LoginRequest;
import tech.impulso.auth.dto.RegisterRequest;
import tech.impulso.auth.dto.ResetPasswordRequest;
import tech.impulso.auth.dto.UserResponse;
import tech.impulso.auth.entity.EmailVerificationToken;
import tech.impulso.auth.entity.PasswordResetToken;
import tech.impulso.auth.repository.EmailVerificationTokenRepository;
import tech.impulso.auth.repository.PasswordResetTokenRepository;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;
import tech.impulso.users.repository.UserRepository;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

/**
 * Servicio principal del módulo de autenticación.
 *
 * <p>Coordina el registro de nuevos usuarios, la verificación de correo,
 * el inicio de sesión, el cierre de sesión y el flujo de recuperación de
 * contraseña. Aplica las reglas funcionales de los requerimientos
 * RF-001, RF-002, RF-003, RF-007, RF-054 y RF-055.</p>
 */
@Service
public class AuthService {

    /**
     * Duración del enlace de verificación de correo. Se mantiene amplia
     * para facilitar el flujo del usuario, pero acotada para reducir la
     * ventana de exposición del token.
     */
    private static final Duration VERIFICATION_TOKEN_TTL = Duration.ofHours(24);

    /**
     * Duración del enlace de recuperación de contraseña. Se mantiene corta
     * para reducir el riesgo de reutilización indebida.
     */
    private static final Duration PASSWORD_RESET_TOKEN_TTL = Duration.ofMinutes(30);

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            UserRepository userRepository,
            EmailVerificationTokenRepository verificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            JwtService jwtService,
            TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * Registra un nuevo usuario en la plataforma.
     *
     * <p>La cuenta se crea en estado {@link UserStatus#PENDIENTE_VERIFICACION}
     * y con rol {@link Role#ESTUDIANTE}. Se genera y envía un enlace de
     * verificación al correo del usuario. La asignación del rol de
     * {@link Role#INSTRUCTOR} o {@link Role#ADMINISTRADOR} corresponde
     * exclusivamente al administrador en flujos posteriores.</p>
     *
     * @param request datos de registro validados por Bean Validation.
     * @return información pública del usuario recién creado.
     */
    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        String username = request.username().trim();

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Ya existe una cuenta registrada con este correo electrónico.");
        }
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException(HttpStatus.CONFLICT, "El nombre de usuario no está disponible.");
        }

        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setRole(Role.ESTUDIANTE);
        user.setStatus(UserStatus.PENDIENTE_VERIFICACION);
        user = userRepository.save(user);

        String tokenValue = generateOpaqueToken();
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(tokenValue);
        token.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plus(VERIFICATION_TOKEN_TTL));
        verificationTokenRepository.save(token);

        emailService.sendVerificationEmail(user.getEmail(), tokenValue);

        return UserResponse.from(user);
    }

    /**
     * Confirma la propiedad de un correo electrónico validando el token
     * de verificación enviado por correo.
     *
     * @param tokenValue valor del token recibido desde el enlace.
     */
    @Transactional
    public void verifyEmail(String tokenValue) {
        EmailVerificationToken token = verificationTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "El enlace de verificación no es válido."));

        if (!token.isUsable()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El enlace de verificación ha expirado o ya fue utilizado.");
        }

        User user = token.getUser();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (user.getStatus() == UserStatus.PENDIENTE_VERIFICACION) {
            user.setStatus(UserStatus.ACTIVA);
            user.setEmailVerifiedAt(now);
            userRepository.save(user);
        }

        token.setConsumedAt(now);
        verificationTokenRepository.save(token);
    }

    /**
     * Reenvía el correo de verificación a un usuario que aún no ha
     * confirmado su cuenta. Por seguridad no se revela si el correo
     * corresponde o no a una cuenta existente.
     *
     * @param email correo electrónico al que reenviar el enlace.
     */
    @Transactional
    public void resendVerification(String email) {
        String normalized = email.trim().toLowerCase();
        userRepository.findByEmail(normalized).ifPresent(user -> {
            if (user.getStatus() != UserStatus.PENDIENTE_VERIFICACION) {
                return;
            }
            verificationTokenRepository.deleteByUser(user);

            String tokenValue = generateOpaqueToken();
            EmailVerificationToken token = new EmailVerificationToken();
            token.setUser(user);
            token.setToken(tokenValue);
            token.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plus(VERIFICATION_TOKEN_TTL));
            verificationTokenRepository.save(token);

            emailService.sendVerificationEmail(user.getEmail(), tokenValue);
        });
    }

    /**
     * Autentica al usuario mediante correo y contraseña y emite un token
     * JWT para las peticiones posteriores.
     *
     * @param request credenciales del usuario.
     * @return respuesta con el token de acceso y la información del usuario.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas."));

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
        }

        switch (user.getStatus()) {
            case PENDIENTE_VERIFICACION -> throw new BusinessException(HttpStatus.FORBIDDEN,
                    "La cuenta aún no ha sido verificada. Revise su correo electrónico para completar el proceso.");
            case DESACTIVADA -> throw new BusinessException(HttpStatus.FORBIDDEN,
                    "La cuenta se encuentra desactivada. Contacte al administrador.");
            case ACTIVA -> { /* continúa el flujo normal */ }
        }

        String tokenId = UUID.randomUUID().toString();
        String accessToken = jwtService.generateToken(user, tokenId);

        user.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(jwtService.getExpiration());
        return new AuthResponse(accessToken, "Bearer", expiresAt, UserResponse.from(user));
    }

    /**
     * Invalida el token de acceso actual del usuario, evitando que pueda
     * seguir utilizándose hasta su expiración natural.
     *
     * @param claims claims decodificados del token activo.
     */
    public void logout(Claims claims) {
        long secondsUntilExpiration = Math.max(0,
                (claims.getExpiration().getTime() - System.currentTimeMillis()) / 1000);
        tokenBlacklistService.blacklist(claims.getId(), secondsUntilExpiration);
    }

    /**
     * Inicia el flujo de recuperación de contraseña emitiendo un token
     * temporal y enviando el enlace correspondiente al correo del usuario.
     * Siempre finaliza sin error para no revelar si el correo se encuentra
     * registrado.
     *
     * @param request solicitud con el correo del usuario.
     */
    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String normalized = request.email().trim().toLowerCase();
        userRepository.findByEmail(normalized).ifPresent(user -> {
            if (user.getStatus() == UserStatus.DESACTIVADA) {
                return;
            }
            passwordResetTokenRepository.deleteByUser(user);

            String tokenValue = generateOpaqueToken();
            PasswordResetToken token = new PasswordResetToken();
            token.setUser(user);
            token.setToken(tokenValue);
            token.setExpiresAt(OffsetDateTime.now(ZoneOffset.UTC).plus(PASSWORD_RESET_TOKEN_TTL));
            passwordResetTokenRepository.save(token);

            emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);
        });
    }

    /**
     * Aplica una nueva contraseña utilizando el token de recuperación
     * previamente emitido.
     *
     * @param request solicitud con el token y la nueva contraseña.
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "El enlace de recuperación no es válido."));

        if (!token.isUsable()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "El enlace de recuperación ha expirado o ya fue utilizado.");
        }

        User user = token.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        token.setConsumedAt(OffsetDateTime.now(ZoneOffset.UTC));
        passwordResetTokenRepository.save(token);
    }

    /**
     * Genera un token opaco criptográficamente aleatorio y seguro,
     * codificado en base64 sin relleno.
     *
     * @return el valor del token.
     */
    private String generateOpaqueToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
