package tech.impulso.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

/**
 * Servicio responsable del envío de correos transaccionales de la
 * plataforma.
 *
 * <p>La integración se realiza mediante {@link JavaMailSender} apuntando
 * al servicio SMTP de Brevo (host, puerto y credenciales se configuran
 * mediante variables de entorno). Los mensajes se envían en formato HTML
 * y siempre incluyen una versión en texto plano de respaldo.</p>
 *
 * <p>Cuando la propiedad {@code app.mail.enabled} está desactivada
 * (típicamente en entornos locales o pruebas), el servicio se limita a
 * registrar el contenido en el log en lugar de contactar al proveedor,
 * facilitando el desarrollo sin credenciales reales.</p>
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String senderEmail;
    private final String senderName;
    private final String verificationBaseUrl;
    private final String passwordResetBaseUrl;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.enabled:false}") boolean enabled,
            @Value("${app.mail.sender:no-reply@impulso.tech}") String senderEmail,
            @Value("${app.mail.sender-name:Impulso Tech}") String senderName,
            @Value("${app.mail.verification-base-url:http://localhost:4200/auth/verify}") String verificationBaseUrl,
            @Value("${app.mail.password-reset-base-url:http://localhost:4200/auth/reset-password}") String passwordResetBaseUrl) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
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
        String subject = "Confirma tu cuenta en Impulso Tech";
        String html = verificationTemplate(link);
        String plainText = """
                Bienvenido a Impulso Tech.

                Confirma tu cuenta abriendo el siguiente enlace:
                %s

                Si tú no creaste la cuenta, puedes ignorar este correo.
                """.formatted(link);
        deliver(toEmail, subject, html, plainText, "VERIFICACION", link);
    }

    /**
     * Envía el correo con el enlace para restablecer la contraseña.
     *
     * @param toEmail correo destinatario.
     * @param token   token de recuperación asociado al usuario.
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String link = "%s?token=%s".formatted(passwordResetBaseUrl, token);
        String subject = "Recuperación de contraseña — Impulso Tech";
        String html = passwordResetTemplate(link);
        String plainText = """
                Recibimos una solicitud para restablecer tu contraseña en Impulso Tech.

                Establece una nueva contraseña abriendo el siguiente enlace:
                %s

                El enlace tiene una vigencia limitada. Si no realizaste la solicitud,
                puedes ignorar este correo.
                """.formatted(link);
        deliver(toEmail, subject, html, plainText, "RECUPERACION", link);
    }

    /**
     * Envía efectivamente el correo o, cuando el envío está desactivado,
     * escribe el contenido esencial en el log para facilitar el
     * desarrollo local.
     *
     * @param toEmail   destinatario.
     * @param subject   asunto.
     * @param html      cuerpo en HTML.
     * @param plainText cuerpo en texto plano.
     * @param logTag    etiqueta corta para identificar el tipo de correo en el log.
     * @param link      enlace principal, útil como referencia rápida en el log.
     */
    private void deliver(String toEmail, String subject, String html, String plainText,
                         String logTag, String link) {
        if (!enabled) {
            log.info("[EMAIL:{}][DESHABILITADO] Destinatario: {} — Enlace: {}", logTag, toEmail, link);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(senderEmail, senderName, StandardCharsets.UTF_8.name()));
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(plainText, html);
            mailSender.send(message);
            log.info("[EMAIL:{}] Enviado correctamente a {}", logTag, toEmail);
        } catch (MessagingException | UnsupportedEncodingException | MailException ex) {
            // No propagamos la excepción para no romper el flujo de negocio
            // (por ejemplo, un registro debe completarse aunque el proveedor
            // de correo falle temporalmente). El usuario podrá solicitar el
            // reenvío del enlace si nunca lo recibe.
            log.error("[EMAIL:{}] Falló el envío a {}: {}", logTag, toEmail, ex.getMessage());
        }
    }

    /**
     * Construye la plantilla HTML del correo de verificación.
     *
     * @param link enlace de verificación ya construido.
     * @return HTML listo para enviar.
     */
    private String verificationTemplate(String link) {
        return """
                <!doctype html>
                <html lang="es">
                  <body style="font-family: Arial, sans-serif; color: #1f2937; background: #f9fafb; padding: 24px;">
                    <div style="max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; border: 1px solid #e5e7eb;">
                      <h1 style="margin: 0 0 16px; font-size: 22px; color: #111827;">Bienvenido a Impulso Tech</h1>
                      <p style="margin: 0 0 16px; line-height: 1.5;">Gracias por unirte a la plataforma. Confirma tu cuenta para comenzar a aprender.</p>
                      <p style="margin: 0 0 24px;">
                        <a href="%s" style="display: inline-block; background: #2563eb; color: #ffffff; padding: 12px 20px; border-radius: 8px; text-decoration: none;">Confirmar mi cuenta</a>
                      </p>
                      <p style="margin: 0 0 8px; font-size: 13px; color: #6b7280;">Si el botón no funciona, copia y pega este enlace en tu navegador:</p>
                      <p style="margin: 0 0 24px; font-size: 13px; word-break: break-all; color: #2563eb;">%s</p>
                      <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;" />
                      <p style="margin: 0; font-size: 12px; color: #9ca3af;">Si tú no creaste la cuenta, puedes ignorar este correo.</p>
                    </div>
                  </body>
                </html>
                """.formatted(link, link);
    }

    /**
     * Construye la plantilla HTML del correo de recuperación de
     * contraseña.
     *
     * @param link enlace de recuperación ya construido.
     * @return HTML listo para enviar.
     */
    private String passwordResetTemplate(String link) {
        return """
                <!doctype html>
                <html lang="es">
                  <body style="font-family: Arial, sans-serif; color: #1f2937; background: #f9fafb; padding: 24px;">
                    <div style="max-width: 560px; margin: 0 auto; background: #ffffff; border-radius: 12px; padding: 32px; border: 1px solid #e5e7eb;">
                      <h1 style="margin: 0 0 16px; font-size: 22px; color: #111827;">Recuperación de contraseña</h1>
                      <p style="margin: 0 0 16px; line-height: 1.5;">Recibimos una solicitud para restablecer la contraseña de tu cuenta en Impulso Tech.</p>
                      <p style="margin: 0 0 24px;">
                        <a href="%s" style="display: inline-block; background: #2563eb; color: #ffffff; padding: 12px 20px; border-radius: 8px; text-decoration: none;">Restablecer contraseña</a>
                      </p>
                      <p style="margin: 0 0 8px; font-size: 13px; color: #6b7280;">Si el botón no funciona, copia y pega este enlace en tu navegador:</p>
                      <p style="margin: 0 0 24px; font-size: 13px; word-break: break-all; color: #2563eb;">%s</p>
                      <p style="margin: 0 0 8px; font-size: 13px; color: #6b7280;">El enlace tiene una vigencia limitada por motivos de seguridad.</p>
                      <hr style="border: none; border-top: 1px solid #e5e7eb; margin: 24px 0;" />
                      <p style="margin: 0; font-size: 12px; color: #9ca3af;">Si tú no solicitaste el cambio, puedes ignorar este correo.</p>
                    </div>
                  </body>
                </html>
                """.formatted(link, link);
    }
}
