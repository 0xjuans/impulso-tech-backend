package tech.impulso.certificates.service;

import org.junit.jupiter.api.Test;
import tech.impulso.certificates.entity.Certificate;
import tech.impulso.courses.entity.Course;
import tech.impulso.users.entity.User;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Prueba que el generador de PDF produce contenido válido (empieza por
 * la firma {@code %PDF-}) y de tamaño razonable. No inspecciona el
 * contenido visual, sólo la estructura mínima.
 */
class CertificatePdfRendererTest {

    @Test
    void generaPdfConFirmaValida() {
        CertificatePdfRenderer renderer = new CertificatePdfRenderer();
        User user = new User();
        user.setFirstName("María");
        user.setLastName("Gómez");
        user.setUsername("mariag");
        user.setEmail("m@test");

        Course course = new Course();
        setId(course, 5L);
        course.setName("Testing avanzado");

        Certificate certificate = new Certificate();
        certificate.setUser(user);
        certificate.setCourse(course);
        certificate.setVerificationCode(UUID.randomUUID());
        certificate.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));

        byte[] pdf = renderer.render(certificate,
                "http://localhost:4200/verify/" + certificate.getVerificationCode(),
                "María Gómez");

        assertNotNull(pdf);
        assertTrue(pdf.length > 500, "el PDF debe tener contenido");
        String header = new String(pdf, 0, 5);
        assertTrue(header.startsWith("%PDF-"), "el PDF debe comenzar con la firma %PDF-");
    }

    private static void setId(Object target, Long id) {
        try {
            Field field = target.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(target, id);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
