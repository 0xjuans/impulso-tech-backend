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
     * Construye la plantilla HTML del correo de verificación con la
     * identidad visual de Impulso Tech: fondo oscuro con acento naranja,
     * wordmark tipo terminal y tipografía monoespaciada para las piezas
     * clave. Los estilos se declaran inline porque los clientes de correo
     * (Gmail, Outlook, Apple Mail) descartan hojas de estilo externas.
     *
     * @param link enlace de verificación ya construido.
     * @return HTML listo para enviar.
     */
    private String verificationTemplate(String link) {
        return brandedTemplate(
                "Confirma tu cuenta",
                "Bienvenido a Impulso Tech",
                "Estamos a un paso de que empieces tu ruta de aprendizaje. Confirma tu cuenta para desbloquear los cursos, retos y laboratorios.",
                "> impulso auth --verify",
                "Confirmar mi cuenta",
                link,
                "El enlace tiene una vigencia limitada. Si tú no creaste la cuenta, puedes ignorar este correo con tranquilidad.");
    }

    /**
     * Construye la plantilla HTML del correo de recuperación de
     * contraseña reutilizando el layout de marca definido en
     * {@link #brandedTemplate}.
     *
     * @param link enlace de recuperación ya construido.
     * @return HTML listo para enviar.
     */
    private String passwordResetTemplate(String link) {
        return brandedTemplate(
                "Recupera tu contraseña",
                "Recuperación de contraseña",
                "Recibimos una solicitud para restablecer la contraseña de tu cuenta en Impulso Tech. Si tú la iniciaste, continúa con el botón.",
                "> impulso auth --reset",
                "Restablecer contraseña",
                link,
                "El enlace expira pronto por seguridad. Si tú no solicitaste el cambio, ignora este correo y tu contraseña seguirá intacta.");
    }

    /**
     * Layout común de los correos transaccionales de Impulso Tech.
     *
     * <p>Renderiza una tarjeta blanca sobre un fondo suave, con el
     * wordmark {@code > Impulso.tech} en la cabecera, un chip tipo
     * terminal con el comando contextual, un botón CTA en naranja y un
     * bloque de enlace en texto plano como fallback. La estructura se
     * apoya en tablas por compatibilidad con clientes de correo antiguos
     * (Outlook 2016 en Windows en particular).</p>
     *
     * @param preheader   texto oculto que aparece como preview del correo.
     * @param heading     titular principal del mensaje.
     * @param body        párrafo introductorio bajo el titular.
     * @param commandChip texto del chip terminal (por ejemplo
     *                    {@code "> impulso auth --verify"}).
     * @param ctaLabel    etiqueta del botón de acción.
     * @param link        URL a la que apunta el botón y el bloque fallback.
     * @param footerNote  aclaración final sobre vigencia o desestimación.
     * @return HTML listo para enviar.
     */
    private String brandedTemplate(String preheader, String heading, String body,
                                   String commandChip, String ctaLabel, String link,
                                   String footerNote) {
        return """
                <!doctype html>
                <html lang="es">
                  <head>
                    <meta charset="utf-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1" />
                    <title>%s</title>
                  </head>
                  <body style="margin:0; padding:0; background:#f6f2ee; font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif; color:#201515;">
                    <div style="display:none; overflow:hidden; line-height:1; opacity:0; max-height:0; max-width:0;">%s</div>
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background:#f6f2ee; padding:32px 16px;">
                      <tr>
                        <td align="center">
                          <table role="presentation" width="560" cellpadding="0" cellspacing="0" style="width:100%%; max-width:560px; background:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 8px 24px rgba(32,21,21,0.06); border:1px solid rgba(32,21,21,0.06);">
                            <tr>
                              <td style="background:#201515; padding:24px 32px;">
                                <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                  <tr>
                                    <td style="font-family:'JetBrains Mono','Fira Code',ui-monospace,SFMono-Regular,Menlo,monospace; font-size:18px; color:#ffffff; letter-spacing:-0.01em;">
                                      <span style="color:#ff4f00;">&gt;</span> Impulso<span style="color:#ff4f00;">.tech</span><span style="display:inline-block; width:8px; height:16px; background:#ff4f00; margin-left:4px; vertical-align:-2px;"></span>
                                    </td>
                                  </tr>
                                </table>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:36px 32px 8px;">
                                <span style="display:inline-block; padding:6px 12px; border-radius:999px; background:#fff2ea; color:#ff4f00; font-family:'JetBrains Mono','Fira Code',ui-monospace,SFMono-Regular,Menlo,monospace; font-size:12px; letter-spacing:0.02em;">%s</span>
                                <h1 style="margin:16px 0 12px; font-size:24px; line-height:1.25; color:#201515; letter-spacing:-0.02em;">%s</h1>
                                <p style="margin:0 0 24px; font-size:15px; line-height:1.6; color:#4a3a3a;">%s</p>
                              </td>
                            </tr>
                            <tr>
                              <td align="center" style="padding:0 32px 8px;">
                                <table role="presentation" cellpadding="0" cellspacing="0">
                                  <tr>
                                    <td style="border-radius:999px; background:#ff4f00;">
                                      <a href="%s" style="display:inline-block; padding:14px 28px; font-size:15px; font-weight:600; color:#ffffff; text-decoration:none; letter-spacing:0.01em;">%s &rarr;</a>
                                    </td>
                                  </tr>
                                </table>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:24px 32px 8px;">
                                <p style="margin:0 0 8px; font-size:13px; color:#7a6a68;">Si el botón no funciona, copia y pega este enlace:</p>
                                <p style="margin:0; font-size:12px; word-break:break-all; font-family:ui-monospace,SFMono-Regular,Menlo,monospace; color:#ff4f00; background:#fff8f4; padding:12px 14px; border-radius:8px; border:1px solid #ffe0d1;">%s</p>
                              </td>
                            </tr>
                            <tr>
                              <td style="padding:16px 32px 32px;">
                                <hr style="border:none; border-top:1px solid rgba(32,21,21,0.08); margin:0 0 16px;" />
                                <p style="margin:0; font-size:12px; line-height:1.5; color:#8a7a78;">%s</p>
                              </td>
                            </tr>
                            <tr>
                              <td style="background:#faf7f4; padding:20px 32px; text-align:center;">
                                <p style="margin:0; font-size:11px; color:#8a7a78;">© Impulso Tech · Aprende programación con retos, IA y comunidad.</p>
                              </td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </table>
                  </body>
                </html>
                """.formatted(preheader, preheader, commandChip, heading, body, link, ctaLabel, link, footerNote);
    }
}
