package tech.impulso.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.auth.dto.AuthResponse;
import tech.impulso.auth.dto.UserResponse;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;
import tech.impulso.users.repository.UserRepository;

import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servicio que autentica a los usuarios mediante Google Identity
 * Services (RF-004).
 *
 * <p>Verifica el ID token emitido por Google contra sus claves públicas,
 * garantizando su autenticidad y vigencia. Cuando el correo asociado ya
 * corresponde a una cuenta registrada, inicia sesión; en caso contrario
 * crea automáticamente una cuenta en estado {@link UserStatus#ACTIVA}
 * (Google ya verifica el correo) con rol {@link Role#ESTUDIANTE}.</p>
 */
@Service
public class GoogleAuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final String clientId;
    private GoogleIdTokenVerifier verifier;

    public GoogleAuthService(UserRepository userRepository,
                             JwtService jwtService,
                             @Value("${app.security.google.client-id:}") String clientId) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.clientId = clientId;
    }

    /**
     * Inicializa el verificador de tokens una vez que el bean está
     * construido. Se posterga a {@code @PostConstruct} para no bloquear
     * el arranque cuando el {@code client-id} aún no está configurado
     * (por ejemplo, en desarrollo local sin OAuth habilitado).
     */
    @PostConstruct
    void init() {
        if (clientId == null || clientId.isBlank()) {
            return;
        }
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();
    }

    /**
     * Autentica al usuario a partir del ID token de Google. Si el
     * correo aún no existe en la plataforma, crea una cuenta nueva con
     * la información suministrada por Google.
     *
     * @param idToken ID token emitido por Google Identity Services.
     * @return respuesta con el token JWT propio y la información pública
     *         del usuario.
     */
    @Transactional
    public AuthResponse loginWithGoogle(String idToken) {
        if (verifier == null) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE,
                    "El inicio de sesión con Google no está habilitado en este entorno.");
        }

        GoogleIdToken.Payload payload = verify(idToken);
        Boolean emailVerified = payload.getEmailVerified();
        if (emailVerified == null || !emailVerified) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED,
                    "El correo asociado a la cuenta de Google no está verificado.");
        }

        String email = payload.getEmail().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email)
                .map(existing -> reactivateIfNeeded(existing))
                .orElseGet(() -> createFromGoogle(payload, email));

        if (user.getStatus() == UserStatus.DESACTIVADA) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "La cuenta se encuentra desactivada. Contacte al administrador.");
        }

        user.setLastLoginAt(OffsetDateTime.now(ZoneOffset.UTC));
        userRepository.save(user);

        String tokenId = UUID.randomUUID().toString();
        String accessToken = jwtService.generateToken(user, tokenId);
        OffsetDateTime expiresAt = OffsetDateTime.now(ZoneOffset.UTC).plus(jwtService.getExpiration());
        return new AuthResponse(accessToken, "Bearer", expiresAt, UserResponse.from(user));
    }

    /**
     * Verifica la firma y los claims del ID token de Google.
     *
     * @param idToken token a verificar.
     * @return payload validado listo para consumir.
     */
    private GoogleIdToken.Payload verify(String idToken) {
        try {
            GoogleIdToken parsed = verifier.verify(idToken);
            if (parsed == null) {
                throw new BusinessException(HttpStatus.UNAUTHORIZED,
                        "El token de Google no es válido o ha expirado.");
            }
            return parsed.getPayload();
        } catch (GeneralSecurityException | java.io.IOException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED,
                    "No fue posible verificar el token de Google.");
        }
    }

    /**
     * Crea una cuenta nueva a partir de la información devuelta por
     * Google. La cuenta se marca como verificada porque Google ya
     * confirmó la titularidad del correo.
     *
     * @param payload información del token de Google.
     * @param email   correo normalizado a minúsculas.
     * @return usuario recién persistido.
     */
    private User createFromGoogle(GoogleIdToken.Payload payload, String email) {
        String givenName = stringClaim(payload, "given_name", "Estudiante");
        String familyName = stringClaim(payload, "family_name", "Impulso");
        String pictureUrl = stringClaim(payload, "picture", null);

        User user = new User();
        user.setEmail(email);
        user.setUsername(generateUniqueUsername(email));
        user.setPasswordHash(null);
        user.setFirstName(givenName);
        user.setLastName(familyName);
        user.setProfilePhotoUrl(pictureUrl);
        user.setRole(Role.ESTUDIANTE);
        user.setStatus(UserStatus.ACTIVA);
        user.setEmailVerifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return userRepository.save(user);
    }

    /**
     * Cuando la cuenta existente aún estaba pendiente de verificación,
     * la activa automáticamente aprovechando la validación realizada por
     * Google.
     *
     * @param user usuario existente.
     * @return el mismo usuario, activado si correspondía.
     */
    private User reactivateIfNeeded(User user) {
        if (user.getStatus() == UserStatus.PENDIENTE_VERIFICACION) {
            user.setStatus(UserStatus.ACTIVA);
            user.setEmailVerifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
        }
        return user;
    }

    /**
     * Genera un nombre de usuario único a partir del correo. Cuando el
     * candidato natural ya está en uso se anexan sufijos numéricos hasta
     * encontrar uno libre.
     *
     * @param email correo del usuario.
     * @return nombre de usuario disponible.
     */
    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0]
                .replaceAll("[^a-zA-Z0-9._-]", "")
                .toLowerCase(Locale.ROOT);
        if (base.length() < 3) {
            base = "usuario";
        }
        if (base.length() > 50) {
            base = base.substring(0, 50);
        }
        String candidate = base;
        int attempts = 0;
        while (userRepository.existsByUsername(candidate)) {
            attempts++;
            candidate = "%s-%d".formatted(base, ThreadLocalRandom.current().nextInt(1000, 9999));
            if (attempts > 10) {
                candidate = "%s-%s".formatted(base, UUID.randomUUID().toString().substring(0, 8));
                break;
            }
        }
        return candidate;
    }

    /**
     * Devuelve un claim de tipo cadena del payload aplicando el valor
     * por defecto cuando no está presente.
     *
     * @param payload      payload del ID token.
     * @param claim        nombre del claim a leer.
     * @param defaultValue valor por defecto si el claim no existe.
     * @return valor del claim o el valor por defecto.
     */
    private String stringClaim(GoogleIdToken.Payload payload, String claim, String defaultValue) {
        Object value = payload.get(claim);
        if (value == null) {
            return defaultValue;
        }
        String text = value.toString().trim();
        return text.isEmpty() ? defaultValue : text;
    }

    /** Duración configurada de los tokens JWT propios. */
    @SuppressWarnings("unused")
    private Duration configuredExpiration() {
        return jwtService.getExpiration();
    }
}
