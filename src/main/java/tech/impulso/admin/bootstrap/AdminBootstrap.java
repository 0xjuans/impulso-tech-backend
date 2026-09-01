package tech.impulso.admin.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tech.impulso.admin.service.AdminActivityLogger;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;
import tech.impulso.users.repository.UserRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Componente responsable de asegurar la existencia de al menos un
 * administrador al arrancar la aplicación.
 *
 * <p>Al no existir usuarios con rol {@link Role#ADMINISTRADOR}, este
 * componente crea uno utilizando las variables de entorno
 * {@code APP_ADMIN_EMAIL}, {@code APP_ADMIN_USERNAME},
 * {@code APP_ADMIN_PASSWORD}, {@code APP_ADMIN_FIRST_NAME} y
 * {@code APP_ADMIN_LAST_NAME}. Si alguna de ellas no está definida el
 * componente registra una advertencia y no crea la cuenta.</p>
 *
 * <p>Una vez creado el primer administrador, éste puede gestionar los
 * demás roles y usuarios desde el panel administrativo, tal como
 * establece el RF-031. La cuenta se crea directamente en estado
 * {@link UserStatus#ACTIVA} porque el bootstrap se ejecuta bajo control
 * del despliegue.</p>
 */
@Component
public class AdminBootstrap implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminActivityLogger activityLogger;
    private final String email;
    private final String username;
    private final String password;
    private final String firstName;
    private final String lastName;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          AdminActivityLogger activityLogger,
                          @Value("${app.admin.email:}") String email,
                          @Value("${app.admin.username:}") String username,
                          @Value("${app.admin.password:}") String password,
                          @Value("${app.admin.first-name:}") String firstName,
                          @Value("${app.admin.last-name:}") String lastName) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityLogger = activityLogger;
        this.email = email;
        this.username = username;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Crea el primer administrador si aún no existe ninguno en la base
     * de datos.
     */
    @Override
    public void run(String... args) {
        if (userRepository.existsByRole(Role.ADMINISTRADOR)) {
            return;
        }

        if (email.isBlank() || username.isBlank() || password.isBlank()
                || firstName.isBlank() || lastName.isBlank()) {
            log.warn("No existe ningún administrador y las variables APP_ADMIN_* no están completas. "
                    + "Defina APP_ADMIN_EMAIL, APP_ADMIN_USERNAME, APP_ADMIN_PASSWORD, "
                    + "APP_ADMIN_FIRST_NAME y APP_ADMIN_LAST_NAME para crear el primer administrador.");
            return;
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            log.warn("Ya existe un usuario con el correo {}. Promuévalo manualmente al rol ADMINISTRADOR.",
                    normalizedEmail);
            return;
        }

        User admin = new User();
        admin.setEmail(normalizedEmail);
        admin.setUsername(username.trim());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setFirstName(firstName.trim());
        admin.setLastName(lastName.trim());
        admin.setRole(Role.ADMINISTRADOR);
        admin.setStatus(UserStatus.ACTIVA);
        admin.setEmailVerifiedAt(OffsetDateTime.now(ZoneOffset.UTC));
        admin = userRepository.save(admin);

        activityLogger.log(
                null,
                AdminActivityLogger.ACTION_ADMIN_BOOTSTRAPPED,
                AdminActivityLogger.TARGET_USER,
                admin.getId(),
                "Creación automática del primer administrador durante el arranque.");

        log.info("Administrador inicial creado: {}", normalizedEmail);
    }
}
