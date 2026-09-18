package tech.impulso.certificates.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import tech.impulso.certificates.entity.Certificate;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Genera un certificado en formato PDF a partir de la entidad
 * {@link Certificate}.
 *
 * <p>La composición es intencionalmente sobria: página horizontal A4,
 * marco, título, nombre del estudiante, curso, fecha y bloque de
 * verificación con el código público. No incluye firma digital ni
 * gráficos externos, lo que evita dependencias adicionales y mantiene
 * la generación rápida (on-the-fly en cada descarga).</p>
 */
@Component
public class CertificatePdfRenderer {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es"));

    /**
     * Renderiza el certificado como un arreglo de bytes PDF listo para
     * ser servido en el cuerpo de una respuesta HTTP.
     *
     * @param certificate       certificado a renderizar.
     * @param verificationUrl   URL pública donde el receptor puede
     *                          verificar el código impreso.
     * @param recipientName     nombre visible del estudiante.
     * @return contenido binario del PDF.
     */
    public byte[] render(Certificate certificate, String verificationUrl, String recipientName) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate(), 48f, 48f, 48f, 48f);
            PdfWriter writer = PdfWriter.getInstance(document, output);
            document.open();

            drawBorder(writer, document);

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 34f, new Color(255, 79, 0));
            Font eyebrowFont = FontFactory.getFont(FontFactory.HELVETICA, 12f, new Color(80, 80, 80));
            Font recipientFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 28f, new Color(32, 21, 21));
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 14f, new Color(60, 60, 60));
            Font courseFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20f, new Color(32, 21, 21));
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA, 10f, new Color(90, 90, 90));
            Font codeFont = FontFactory.getFont(FontFactory.COURIER, 10f, new Color(30, 30, 30));

            document.add(spacer(20f));
            document.add(centered("IMPULSO TECH", eyebrowFont));
            document.add(spacer(10f));
            document.add(centered("Certificado de finalización", titleFont));
            document.add(spacer(24f));
            document.add(centered("Se otorga el presente certificado a", bodyFont));
            document.add(spacer(14f));
            document.add(centered(recipientName, recipientFont));
            document.add(spacer(14f));
            document.add(centered("por haber completado satisfactoriamente el curso", bodyFont));
            document.add(spacer(10f));
            document.add(centered("\"" + certificate.getCourse().getName() + "\"", courseFont));
            document.add(spacer(24f));
            document.add(centered("Emitido el " + DATE_FORMATTER.format(certificate.getIssuedAt()), bodyFont));

            document.add(spacer(40f));
            document.add(centered("Código de verificación", smallFont));
            document.add(centered(certificate.getVerificationCode().toString(), codeFont));
            document.add(spacer(4f));
            document.add(centered("Verificar en: " + verificationUrl, smallFont));

            document.close();
            writer.close();
            return output.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("No fue posible generar el certificado en PDF.", e);
        }
    }

    /**
     * Dibuja un marco decorativo alrededor de la página.
     */
    private static void drawBorder(PdfWriter writer, Document document) {
        PdfContentByte canvas = writer.getDirectContent();
        Rectangle page = document.getPageSize();
        float margin = 24f;
        canvas.saveState();
        canvas.setColorStroke(new Color(255, 79, 0));
        canvas.setLineWidth(3f);
        canvas.rectangle(margin, margin,
                page.getWidth() - 2 * margin,
                page.getHeight() - 2 * margin);
        canvas.stroke();
        canvas.setColorStroke(new Color(200, 200, 200));
        canvas.setLineWidth(0.8f);
        canvas.rectangle(margin + 6, margin + 6,
                page.getWidth() - 2 * (margin + 6),
                page.getHeight() - 2 * (margin + 6));
        canvas.stroke();
        canvas.restoreState();
    }

    private static Paragraph centered(String text, Font font) {
        Paragraph paragraph = new Paragraph(new Phrase(text, font));
        paragraph.setAlignment(Element.ALIGN_CENTER);
        return paragraph;
    }

    private static Paragraph spacer(float height) {
        Paragraph spacer = new Paragraph(" ");
        spacer.setSpacingAfter(height);
        return spacer;
    }
}
