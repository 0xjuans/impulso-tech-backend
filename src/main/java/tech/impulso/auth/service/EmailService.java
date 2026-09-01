package tech.impulso.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Servicio responsable del envío de correos transaccionales de la
 * plataforma.
 *
 * <p>Esta implementación inicial simula el envío escribiendo los enlaces
 * en el log del servidor, con el propósito de permitir el desarrollo del
 * flujo completo de autenticación antes de configurar el proveedor real
 * (Brevo). Cuando la integración con Brevo esté disponible, esta clase
 * se sustituirá por una implementación que envíe efectivamente los
 * correos.</p>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final String verificationBaseUrl;
    private final String passwordResetBaseUrl;

    public EmailService(
            @Value("${app.mail.verification-base-url:http://localhost:4200/auth/verify}") String verificationBaseUrl,
            @Value("${app.mail.password-reset-base-url:http://localhost:4200/auth/reset-password}") String passwordResetBaseUrl) {
        this.verificationBaseUrl = verificationBaseUrl;
        this.passwordResetBaseUrl = passwordResetBaseUrl;
    }

    /**
     * Envía el correo de verificación de cuenta al usuario recién
     * registrado.
     *
     * @param toEmail correo destinatario.
     * @param token   token de verificación asociado al usuario.
     */
    public void sendVerificationEmail(String toEmail, String token) {
        String link = "%s?token=%s".formatted(verificationBaseUrl, token);
        log.info("[EMAIL:VERIFICACION] Enviando a {} enlace: {}", toEmail, link);
    }

    /**
     * Envía el correo con el enlace para restablecer la contraseña.
     *
     * @param toEmail correo destinatario.
     * @param token   token de recuperación asociado al usuario.
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = "%s?token=%s".formatted(passwordResetBaseUrl, token);
        log.info("[EMAIL:RECUPERACION] Enviando a {} enlace: {}", toEmail, link);
    }
}
