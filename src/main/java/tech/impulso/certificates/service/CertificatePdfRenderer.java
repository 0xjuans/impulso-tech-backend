package tech.impulso.certificates.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.impulso.certificates.entity.Certificate;
import tech.impulso.courses.entity.Course;
import tech.impulso.users.entity.User;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;

/**
 * Genera un certificado profesional en formato PDF (RF-047).
 *
 * <p>El certificado se dibuja con posicionamiento absoluto sobre una
 * página A4 horizontal para garantizar que todo el contenido cabe en
 * una sola página. Está pensado para verse como un diploma físico:
 * fondo tipo pergamino, doble marco, ornamentos en las esquinas,
 * jerarquía tipográfica clara y bloque inferior con firma, fecha y
 * código de verificación.</p>
 *
 * <p>Si el instructor del curso tiene una firma cargada, se dibuja
 * sobre la línea de firma; de lo contrario se muestra únicamente el
 * nombre del instructor bajo la línea.</p>
 */
@Component
public class CertificatePdfRenderer {

    private static final Logger log = LoggerFactory.getLogger(CertificatePdfRenderer.class);

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es"));

    /** Paleta de la marca. */
    private static final Color BRAND_PRIMARY = new Color(255, 79, 0);
    private static final Color BRAND_INK = new Color(32, 21, 21);
    private static final Color BRAND_INK_SOFT = new Color(74, 58, 58);
    private static final Color BRAND_ACCENT_GOLD = new Color(198, 155, 96);
    private static final Color BRAND_PAPER = new Color(252, 249, 244);
    private static final Color BRAND_MUTED = new Color(120, 110, 105);

    /**
     * Renderiza el certificado como un arreglo de bytes PDF listo para
     * ser servido en el cuerpo de una respuesta HTTP.
     *
     * @param certificate     certificado a renderizar.
     * @param verificationUrl URL pública donde el receptor puede
     *                        verificar el código impreso.
     * @param recipientName   nombre visible del estudiante.
     * @return contenido binario del PDF.
     */
    public byte[] render(Certificate certificate, String verificationUrl, String recipientName) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Rectangle pageSize = PageSize.A4.rotate();
            Document document = new Document(pageSize, 0, 0, 0, 0);
            PdfWriter writer = PdfWriter.getInstance(document, output);
            document.open();
            PdfContentByte canvas = writer.getDirectContent();

            float width = pageSize.getWidth();
            float height = pageSize.getHeight();

            drawBackground(canvas, width, height);
            drawFrame(canvas, width, height);
            drawCornerOrnaments(canvas, width, height);
            drawWatermark(canvas, width, height);
            drawContent(canvas, certificate, recipientName, verificationUrl, width, height);

            document.close();
            writer.close();
            return output.toByteArray();
        } catch (DocumentException | java.io.IOException e) {
            throw new IllegalStateException("No fue posible generar el certificado en PDF.", e);
        }
    }

    /* ---------------------------------------------------- Composición */

    private void drawContent(PdfContentByte canvas, Certificate cert,
                             String recipientName, String verificationUrl,
                             float w, float h) {
        Course course = cert.getCourse();
        float centerX = w / 2f;

        // Wordmark superior "> Impulso.tech".
        drawWordmark(canvas, centerX, h - 60f);

        // Línea dorada bajo el wordmark.
        drawDividerLine(canvas, centerX - 70f, centerX + 70f, h - 80f, BRAND_ACCENT_GOLD, 0.6f);

        // Eyebrow con letter-spacing.
        text(canvas, "CERTIFICADO DE FINALIZACIÓN",
                helvetica(true), 11.5f, BRAND_MUTED,
                centerX, h - 108f, PdfContentByte.ALIGN_CENTER, 6f);

        // Título principal.
        text(canvas, "Reconocimiento de logro",
                times(true), 34f, BRAND_INK,
                centerX, h - 152f, PdfContentByte.ALIGN_CENTER, 0f);

        // Diamantes decorativos DEBAJO del título como separadores.
        drawDiamondSeparators(canvas, centerX, h - 178f);

        // Subtítulo.
        text(canvas, "Se otorga el presente reconocimiento a",
                helvetica(false), 12.5f, BRAND_INK_SOFT,
                centerX, h - 208f, PdfContentByte.ALIGN_CENTER, 0f);

        // Nombre del estudiante (protagonista).
        text(canvas, recipientName,
                times(true), 36f, BRAND_PRIMARY,
                centerX, h - 258f, PdfContentByte.ALIGN_CENTER, 0f);

        // Línea decorativa dorada bajo el nombre.
        drawDividerLine(canvas, centerX - 230f, centerX + 230f, h - 272f, BRAND_ACCENT_GOLD, 0.5f);

        // "por completar exitosamente el curso"
        text(canvas, "por completar exitosamente el curso",
                helvetica(false), 12.5f, BRAND_INK_SOFT,
                centerX, h - 300f, PdfContentByte.ALIGN_CENTER, 0f);

        // Nombre del curso entre comillas tipográficas.
        String courseName = course.getName() == null ? "" : course.getName();
        text(canvas, "«" + courseName + "»",
                times(true), 22f, BRAND_INK,
                centerX, h - 338f, PdfContentByte.ALIGN_CENTER, 0f);

        // Chips de metadatos.
        drawMetaChips(canvas, course, centerX, h - 378f);

        // Bloque inferior.
        drawFooter(canvas, cert, verificationUrl, w);
    }

    /**
     * Dibuja el wordmark "&gt; Impulso.tech" centrado en la coordenada
     * indicada, imitando el estilo de la marca en la aplicación.
     */
    private void drawWordmark(PdfContentByte canvas, float centerX, float baselineY) {
        Font monoFont = FontFactory.getFont(FontFactory.COURIER_BOLD, 16f, BRAND_INK);
        BaseFont mono = monoFont.getCalculatedBaseFont(false);

        String prompt = "> ";
        String impulso = "Impulso";
        String dot = ".";
        String tech = "tech";

        float wPrompt = mono.getWidthPoint(prompt, 16f);
        float wImpulso = mono.getWidthPoint(impulso, 16f);
        float wDot = mono.getWidthPoint(dot, 16f);
        float wTech = mono.getWidthPoint(tech, 16f);
        float total = wPrompt + wImpulso + wDot + wTech + 8f; // 8 para el cursor
        float x = centerX - total / 2f;

        canvas.beginText();
        canvas.setColorFill(BRAND_PRIMARY);
        canvas.setFontAndSize(mono, 16f);
        canvas.setTextMatrix(x, baselineY);
        canvas.showText(prompt);
        canvas.setColorFill(BRAND_INK);
        canvas.showText(impulso);
        canvas.setColorFill(BRAND_PRIMARY);
        canvas.showText(dot + tech);
        canvas.endText();

        // Cursor de terminal (bloque naranja).
        canvas.saveState();
        canvas.setColorFill(BRAND_PRIMARY);
        canvas.rectangle(x + wPrompt + wImpulso + wDot + wTech + 2f, baselineY - 1f, 6f, 12f);
        canvas.fill();
        canvas.restoreState();
    }

    private void drawDiamondSeparators(PdfContentByte canvas, float centerX, float y) {
        // Diamante central + dos líneas cortas doradas a los lados.
        drawDiamond(canvas, centerX, y, 4f, BRAND_PRIMARY);
        drawDividerLine(canvas, centerX - 70f, centerX - 12f, y, BRAND_ACCENT_GOLD, 0.5f);
        drawDividerLine(canvas, centerX + 12f, centerX + 70f, y, BRAND_ACCENT_GOLD, 0.5f);
    }

    private void drawMetaChips(PdfContentByte canvas, Course course, float centerX, float baselineY) {
        String[] chips = new String[3];
        int count = 0;
        if (course.getTechnology() != null && !course.getTechnology().isBlank()) {
            chips[count++] = course.getTechnology();
        }
        if (course.getEstimatedDurationHours() != null && course.getEstimatedDurationHours() > 0) {
            chips[count++] = course.getEstimatedDurationHours() + " horas";
        }
        if (course.getDifficulty() != null) {
            chips[count++] = readable(course.getDifficulty().name());
        }
        if (count == 0) return;

        Font chipFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9.5f, BRAND_INK_SOFT);
        BaseFont bf = chipFont.getCalculatedBaseFont(false);
        float gap = 10f;
        float paddingH = 12f;
        float chipHeight = 22f;
        float totalWidth = 0f;
        float[] widths = new float[count];
        for (int i = 0; i < count; i++) {
            widths[i] = bf.getWidthPoint(chips[i], 9.5f) + 2 * paddingH;
            totalWidth += widths[i];
        }
        totalWidth += gap * (count - 1);

        float x = centerX - totalWidth / 2f;
        float y = baselineY - chipHeight / 2f;
        for (int i = 0; i < count; i++) {
            drawChip(canvas, x, y, widths[i], chipHeight, chips[i], bf);
            x += widths[i] + gap;
        }
    }

    private void drawChip(PdfContentByte canvas, float x, float y, float w, float h,
                          String text, BaseFont font) {
        canvas.saveState();
        canvas.setColorFill(new Color(255, 242, 234));
        canvas.setColorStroke(new Color(255, 79, 0, 60));
        canvas.setLineWidth(0.6f);
        canvas.roundRectangle(x, y, w, h, h / 2f);
        canvas.fillStroke();
        canvas.restoreState();

        canvas.beginText();
        canvas.setColorFill(BRAND_PRIMARY);
        canvas.setFontAndSize(font, 9.5f);
        canvas.showTextAligned(PdfContentByte.ALIGN_CENTER, text,
                x + w / 2f, y + h / 2f - 3.2f, 0);
        canvas.endText();
    }

    private void drawFooter(PdfContentByte canvas, Certificate cert, String verificationUrl, float w) {
        Course course = cert.getCourse();
        User instructor = course.getInstructor();
        String instructorName = displayName(instructor);
        String issuedDate = DATE_FORMATTER.format(cert.getIssuedAt());
        String code = cert.getVerificationCode().toString();

        // Divisor superior del footer.
        drawDividerLine(canvas, 90f, w - 90f, 150f, BRAND_ACCENT_GOLD, 0.4f);

        float footerCenterY = 110f;
        float leftX = 130f;
        float centerX = w / 2f;
        float rightX = w - 130f;

        // Izquierda: fecha de emisión.
        text(canvas, "FECHA DE EMISIÓN", helvetica(true), 8.5f, BRAND_MUTED,
                leftX, footerCenterY + 18f, PdfContentByte.ALIGN_LEFT, 4f);
        text(canvas, issuedDate, helvetica(false), 12f, BRAND_INK,
                leftX, footerCenterY, PdfContentByte.ALIGN_LEFT, 0f);

        // Centro: firma del instructor (imagen si existe) + línea + nombre + rol.
        float signatureLineY = footerCenterY + 8f;
        drawInstructorSignatureImage(canvas, instructor, centerX, signatureLineY);
        drawSignatureLine(canvas, centerX - 90f, centerX + 90f, signatureLineY);
        text(canvas, instructorName != null ? instructorName : "Equipo Impulso Tech",
                times(true), 11f, BRAND_INK,
                centerX, footerCenterY - 8f, PdfContentByte.ALIGN_CENTER, 0f);
        text(canvas, "INSTRUCTOR RESPONSABLE",
                helvetica(true), 8.5f, BRAND_MUTED,
                centerX, footerCenterY - 22f, PdfContentByte.ALIGN_CENTER, 4f);

        // Derecha: código de verificación.
        text(canvas, "CÓDIGO DE VERIFICACIÓN", helvetica(true), 8.5f, BRAND_MUTED,
                rightX, footerCenterY + 18f, PdfContentByte.ALIGN_RIGHT, 4f);
        text(canvas, code, courier(false), 9.5f, BRAND_INK,
                rightX, footerCenterY, PdfContentByte.ALIGN_RIGHT, 0f);

        // URL de verificación centrada bajo el footer para no chocar con esquinas.
        if (verificationUrl != null && !verificationUrl.isBlank()) {
            text(canvas, "Verifica su autenticidad en " + verificationUrl,
                    helvetica(false), 8f, BRAND_MUTED,
                    w / 2f, 70f, PdfContentByte.ALIGN_CENTER, 0f);
        }
    }

    /**
     * Dibuja la firma escaneada/dibujada del instructor sobre la línea
     * de firma, si el instructor la tiene cargada. Silenciosamente
     * ignora imágenes con formato inválido o desmedidas para no romper
     * la generación del PDF.
     */
    private void drawInstructorSignatureImage(PdfContentByte canvas, User instructor,
                                              float centerX, float signatureLineY) {
        if (instructor == null) return;
        String raw = instructor.getSignatureImageUrl();
        if (raw == null || raw.isBlank()) return;
        String data = raw.trim();
        // Aceptamos data URL de tipo image/png o image/jpeg.
        int comma = data.indexOf(',');
        if (!data.startsWith("data:image/") || comma < 0) return;
        try {
            byte[] bytes = Base64.getDecoder().decode(data.substring(comma + 1));
            Image signature = Image.getInstance(bytes);
            // Ajustamos manteniendo la relación de aspecto en una caja
            // de 180×46 puntos, y la centramos justo encima de la línea.
            float boxWidth = 180f;
            float boxHeight = 46f;
            signature.scaleToFit(boxWidth, boxHeight);
            float x = centerX - signature.getScaledWidth() / 2f;
            float y = signatureLineY + 4f;
            signature.setAbsolutePosition(x, y);
            canvas.addImage(signature);
        } catch (Exception e) {
            log.debug("Ignorando firma inválida del instructor {}: {}", instructor.getId(), e.getMessage());
        }
    }

    /* --------------------------------------------------- Ornamentos */

    private void drawBackground(PdfContentByte canvas, float w, float h) {
        canvas.saveState();
        canvas.setColorFill(BRAND_PAPER);
        canvas.rectangle(0f, 0f, w, h);
        canvas.fill();
        canvas.restoreState();
    }

    private void drawFrame(PdfContentByte canvas, float w, float h) {
        float margin = 26f;
        canvas.saveState();
        canvas.setColorStroke(BRAND_PRIMARY);
        canvas.setLineWidth(2.5f);
        canvas.rectangle(margin, margin, w - 2 * margin, h - 2 * margin);
        canvas.stroke();
        canvas.restoreState();

        float inner = margin + 7f;
        canvas.saveState();
        canvas.setColorStroke(BRAND_ACCENT_GOLD);
        canvas.setLineWidth(0.75f);
        canvas.rectangle(inner, inner, w - 2 * inner, h - 2 * inner);
        canvas.stroke();
        canvas.restoreState();
    }

    private void drawCornerOrnaments(PdfContentByte canvas, float w, float h) {
        float m = 26f;
        float size = 26f;
        drawCornerWedge(canvas, m, m, size, 0);
        drawCornerWedge(canvas, w - m, m, size, 90);
        drawCornerWedge(canvas, w - m, h - m, size, 180);
        drawCornerWedge(canvas, m, h - m, size, 270);
    }

    private void drawCornerWedge(PdfContentByte canvas, float x, float y, float size, int rotationDeg) {
        canvas.saveState();
        canvas.setColorFill(BRAND_PRIMARY);
        double rad = Math.toRadians(rotationDeg);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        float dx = size * cos;
        float dy = size * sin;
        float ex = -size * sin;
        float ey = size * cos;
        canvas.moveTo(x, y);
        canvas.lineTo(x + dx, y + dy);
        canvas.lineTo(x + dx / 2f, y + dy / 2f + ey / 2f);
        canvas.lineTo(x + ex / 2f + dx / 2f, y + ey / 2f + dy / 2f);
        canvas.lineTo(x + ex, y + ey);
        canvas.closePath();
        canvas.fill();
        canvas.restoreState();

        float diamondX = x + (size / 1.6f) * cos + (size / 1.6f) * (-sin);
        float diamondY = y + (size / 1.6f) * sin + (size / 1.6f) * cos;
        drawDiamond(canvas, diamondX, diamondY, 3.2f, BRAND_ACCENT_GOLD);
    }

    private void drawDiamond(PdfContentByte canvas, float cx, float cy, float halfSize, Color color) {
        canvas.saveState();
        canvas.setColorFill(color);
        canvas.moveTo(cx, cy + halfSize);
        canvas.lineTo(cx + halfSize, cy);
        canvas.lineTo(cx, cy - halfSize);
        canvas.lineTo(cx - halfSize, cy);
        canvas.closePath();
        canvas.fill();
        canvas.restoreState();
    }

    private void drawWatermark(PdfContentByte canvas, float w, float h) {
        // Monograma "IT" muy suave detrás del contenido.
        canvas.saveState();
        canvas.setColorFill(new Color(32, 21, 21, 9));
        Font wm = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 260f);
        BaseFont bf = wm.getCalculatedBaseFont(false);
        canvas.beginText();
        canvas.setFontAndSize(bf, 260f);
        canvas.showTextAligned(PdfContentByte.ALIGN_CENTER, "IT",
                w / 2f, h / 2f - 85f, 0);
        canvas.endText();
        canvas.restoreState();
    }

    private void drawDividerLine(PdfContentByte canvas, float x1, float x2, float y,
                                 Color color, float width) {
        canvas.saveState();
        canvas.setColorStroke(color);
        canvas.setLineWidth(width);
        canvas.moveTo(x1, y);
        canvas.lineTo(x2, y);
        canvas.stroke();
        canvas.restoreState();
    }

    private void drawSignatureLine(PdfContentByte canvas, float x1, float x2, float y) {
        canvas.saveState();
        canvas.setColorStroke(BRAND_INK);
        canvas.setLineWidth(0.8f);
        canvas.moveTo(x1, y);
        canvas.lineTo(x2, y);
        canvas.stroke();
        canvas.restoreState();
    }

    /* ----------------------------------------------------- Utilidades */

    private void text(PdfContentByte canvas, String text, Font font, float size,
                      Color color, float x, float y, int alignment, float letterSpacing) {
        BaseFont bf = font.getCalculatedBaseFont(false);
        canvas.beginText();
        canvas.setColorFill(color);
        canvas.setFontAndSize(bf, size);
        if (letterSpacing != 0f) {
            canvas.setCharacterSpacing(letterSpacing);
        }
        canvas.showTextAligned(alignment, text == null ? "" : text, x, y, 0);
        canvas.endText();
        if (letterSpacing != 0f) {
            canvas.setCharacterSpacing(0f);
        }
    }

    private static Font helvetica(boolean bold) {
        return FontFactory.getFont(bold ? FontFactory.HELVETICA_BOLD : FontFactory.HELVETICA);
    }

    private static Font times(boolean bold) {
        return FontFactory.getFont(bold ? FontFactory.TIMES_BOLD : FontFactory.TIMES_ROMAN);
    }

    private static Font courier(boolean bold) {
        return FontFactory.getFont(bold ? FontFactory.COURIER_BOLD : FontFactory.COURIER);
    }

    private static String displayName(User user) {
        if (user == null) return null;
        String first = user.getFirstName();
        String last = user.getLastName();
        String composed = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        return composed.isBlank() ? user.getUsername() : composed;
    }

    private static String readable(String enumValue) {
        if (enumValue == null) return "";
        String lower = enumValue.replace('_', ' ').toLowerCase(new Locale("es"));
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
