package tech.impulso.certificates.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;
import tech.impulso.certificates.entity.Certificate;
import tech.impulso.courses.entity.Course;
import tech.impulso.users.entity.User;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Genera un certificado profesional en formato PDF (RF-047).
 *
 * <p>El certificado se dibuja con posicionamiento absoluto sobre una
 * página A4 horizontal para garantizar que todo el contenido cabe en
 * una sola página sin importar la longitud del texto. La composición
 * incluye:</p>
 * <ul>
 *   <li>Marco doble con acento naranja y filete interior dorado.</li>
 *   <li>Wordmark superior "Impulso.tech" con estilo terminal.</li>
 *   <li>Título grande, nombre del estudiante y curso jerarquizados.</li>
 *   <li>Bloque inferior con metadatos, firma del instructor y código
 *       de verificación en línea monoespaciada.</li>
 *   <li>Ornamentos decorativos en las esquinas.</li>
 * </ul>
 */
@Component
public class CertificatePdfRenderer {

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

        // Wordmark superior: "> impulso.tech"
        float wordmarkY = h - 78f;
        drawWordmark(canvas, w / 2f, wordmarkY);

        // Divisor superior con la línea dorada muy fina.
        drawDividerLine(canvas, w / 2f - 60f, w / 2f + 60f, h - 96f, BRAND_ACCENT_GOLD, 0.6f);

        // "CERTIFICADO DE FINALIZACIÓN"
        text(canvas, "CERTIFICADO DE FINALIZACIÓN",
                helvetica(true), 12.5f, BRAND_MUTED,
                w / 2f, h - 122f, PdfContentByte.ALIGN_CENTER, 6f);

        // Título principal grande
        text(canvas, "Reconocimiento de logro",
                times(true), 32f, BRAND_INK,
                w / 2f, h - 168f, PdfContentByte.ALIGN_CENTER, 0f);

        // Ornamentos alrededor del título: dos diamantes pequeños.
        drawDiamond(canvas, w / 2f - 165f, h - 158f, 4.5f, BRAND_PRIMARY);
        drawDiamond(canvas, w / 2f + 165f, h - 158f, 4.5f, BRAND_PRIMARY);

        // Subtítulo
        text(canvas, "Se otorga el presente reconocimiento a",
                helvetica(false), 12.5f, BRAND_INK_SOFT,
                w / 2f, h - 205f, PdfContentByte.ALIGN_CENTER, 0f);

        // Nombre del estudiante con serif elegante
        text(canvas, recipientName,
                times(true), 34f, BRAND_PRIMARY,
                w / 2f, h - 258f, PdfContentByte.ALIGN_CENTER, 0f);

        // Línea decorativa debajo del nombre
        drawDividerLine(canvas, w / 2f - 210f, w / 2f + 210f, h - 268f, BRAND_ACCENT_GOLD, 0.5f);

        // "por completar exitosamente el curso"
        text(canvas, "por completar exitosamente el curso",
                helvetica(false), 12.5f, BRAND_INK_SOFT,
                w / 2f, h - 296f, PdfContentByte.ALIGN_CENTER, 0f);

        // Nombre del curso (con comillas tipográficas)
        String courseName = course.getName() == null ? "" : course.getName();
        text(canvas, "«" + courseName + "»",
                times(true), 22f, BRAND_INK,
                w / 2f, h - 334f, PdfContentByte.ALIGN_CENTER, 0f);

        // Chips de metadatos (tecnología / horas / dificultad)
        drawMetaChips(canvas, course, w / 2f, h - 370f);

        // Bloque inferior: firma + fecha + verificación en 3 columnas
        drawFooter(canvas, cert, verificationUrl, w, h);
    }

    /**
     * Dibuja el wordmark "&gt; Impulso.tech" centrado en la coordenada
     * indicada, imitando el estilo de la marca en la aplicación.
     */
    private void drawWordmark(PdfContentByte canvas, float centerX, float baselineY) {
        // Como es una composición horizontal, computamos el ancho para centrar.
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
        float total = wPrompt + wImpulso + wDot + wTech;
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

        // Cursor de terminal parpadeante (bloque naranja) al final.
        canvas.saveState();
        canvas.setColorFill(BRAND_PRIMARY);
        canvas.rectangle(x + total + 2f, baselineY - 1f, 6f, 12f);
        canvas.fill();
        canvas.restoreState();
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
        float paddingH = 10f;
        float chipHeight = 20f;
        // Ancho total para centrar.
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
        canvas.setLineWidth(0.5f);
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

    private void drawFooter(PdfContentByte canvas, Certificate cert,
                            String verificationUrl, float w, float h) {
        Course course = cert.getCourse();
        String instructorName = displayName(course.getInstructor());
        String issuedDate = DATE_FORMATTER.format(cert.getIssuedAt());
        String code = cert.getVerificationCode().toString();

        // Franja horizontal separadora
        drawDividerLine(canvas, 90f, w - 90f, 100f, BRAND_ACCENT_GOLD, 0.4f);

        // Columna izquierda: fecha de emisión
        float leftX = 130f;
        text(canvas, "FECHA DE EMISIÓN", helvetica(true), 8.5f, BRAND_MUTED,
                leftX, 82f, PdfContentByte.ALIGN_LEFT, 4f);
        text(canvas, issuedDate, helvetica(false), 12f, BRAND_INK,
                leftX, 68f, PdfContentByte.ALIGN_LEFT, 0f);

        // Columna central: firma del instructor
        float centerX = w / 2f;
        drawSignatureLine(canvas, centerX - 70f, centerX + 70f, 78f);
        text(canvas, instructorName != null ? instructorName : "Equipo Impulso Tech",
                times(true), 11f, BRAND_INK,
                centerX, 63f, PdfContentByte.ALIGN_CENTER, 0f);
        text(canvas, "INSTRUCTOR RESPONSABLE",
                helvetica(true), 8.5f, BRAND_MUTED,
                centerX, 51f, PdfContentByte.ALIGN_CENTER, 0f);

        // Columna derecha: código de verificación
        float rightX = w - 130f;
        text(canvas, "CÓDIGO DE VERIFICACIÓN", helvetica(true), 8.5f, BRAND_MUTED,
                rightX, 82f, PdfContentByte.ALIGN_RIGHT, 4f);
        text(canvas, code, courier(false), 9.5f, BRAND_INK,
                rightX, 68f, PdfContentByte.ALIGN_RIGHT, 0f);
        if (verificationUrl != null && !verificationUrl.isBlank()) {
            text(canvas, "Verifica en: " + verificationUrl,
                    helvetica(false), 8f, BRAND_MUTED,
                    rightX, 54f, PdfContentByte.ALIGN_RIGHT, 0f);
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
        // Marco naranja exterior
        canvas.saveState();
        canvas.setColorStroke(BRAND_PRIMARY);
        canvas.setLineWidth(2.5f);
        canvas.rectangle(margin, margin, w - 2 * margin, h - 2 * margin);
        canvas.stroke();
        canvas.restoreState();

        // Filete interior dorado más fino
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
        // Cuatro esquinas: pequeñas cuñas naranjas + diamante interior.
        drawCornerWedge(canvas, m, m, size, 0);
        drawCornerWedge(canvas, w - m, m, size, 90);
        drawCornerWedge(canvas, w - m, h - m, size, 180);
        drawCornerWedge(canvas, m, h - m, size, 270);
    }

    private void drawCornerWedge(PdfContentByte canvas, float x, float y, float size, int rotationDeg) {
        // Cuñas triangulares que "abrazan" cada esquina desde el marco.
        canvas.saveState();
        canvas.setColorFill(BRAND_PRIMARY);
        double rad = Math.toRadians(rotationDeg);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        // Dos triángulos formando una L rellena.
        float dx = size * cos - 0f * sin;
        float dy = size * sin + 0f * cos;
        float ex = 0f * cos - size * sin;
        float ey = 0f * sin + size * cos;
        canvas.moveTo(x, y);
        canvas.lineTo(x + dx, y + dy);
        canvas.lineTo(x + dx / 2f, y + dy / 2f + ey / 2f);
        canvas.lineTo(x + ex / 2f + dx / 2f, y + ey / 2f + dy / 2f);
        canvas.lineTo(x + ex, y + ey);
        canvas.closePath();
        canvas.fill();
        canvas.restoreState();

        // Diamante decorativo en el interior de la esquina.
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
        // Monograma "IT" enorme centrado en gris muy suave detrás del contenido.
        canvas.saveState();
        canvas.setColorFill(new Color(32, 21, 21, 10));
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
            // Reset para no filtrar espacio a otros textos posteriores.
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
