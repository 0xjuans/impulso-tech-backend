package tech.impulso.certificates.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tech.impulso.certificates.dto.CertificateResponse;
import tech.impulso.certificates.dto.CertificateVerificationResponse;
import tech.impulso.certificates.entity.Certificate;
import tech.impulso.certificates.repository.CertificateRepository;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.courses.entity.Course;
import tech.impulso.users.entity.User;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias del {@link CertificateService} apoyadas en un
 * {@link CertificateRepository} mockeado con Mockito.
 *
 * <p>Se verifican las tres reglas clave del módulo (RF-047):
 * <ol>
 *   <li>La emisión sólo procede cuando el curso genera certificado.</li>
 *   <li>La emisión es idempotente por (usuario, curso).</li>
 *   <li>La verificación pública devuelve datos y falla con 404 si no
 *       existe el código.</li>
 * </ol>
 * </p>
 */
class CertificateServiceTest {

    private CertificateRepository repository;
    private RecordingPdfRenderer renderer;
    private CertificateService service;
    private User user;

    /**
     * Almacenamiento en memoria compartido entre stubs de Mockito para
     * simular la persistencia sin implementar toda la interfaz JPA.
     */
    private Map<Long, Certificate> storage;
    private AtomicLong sequence;

    @BeforeEach
    void setUp() {
        storage = new HashMap<>();
        sequence = new AtomicLong(100);
        repository = mock(CertificateRepository.class);
        renderer = new RecordingPdfRenderer();
        service = new CertificateService(
                repository,
                renderer,
                "http://localhost:4200/verify",
                mock(tech.impulso.courses.repository.CourseRepository.class),
                mock(tech.impulso.enrollments.repository.EnrollmentRepository.class));
        user = newUser(1L, "Ana", "Pérez", "anap");

        // Simulamos save() persistiendo en el mapa y asignando id / defaults.
        when(repository.save(any(Certificate.class))).thenAnswer(inv -> {
            Certificate c = inv.getArgument(0);
            if (c.getId() == null) {
                setField(c, "id", sequence.getAndIncrement());
            }
            if (c.getVerificationCode() == null) {
                c.setVerificationCode(UUID.randomUUID());
            }
            if (c.getIssuedAt() == null) {
                c.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));
            }
            storage.put(c.getId(), c);
            return c;
        });
        when(repository.existsByUserIdAndCourseId(anyLong(), anyLong())).thenAnswer(inv -> {
            long userId = inv.getArgument(0);
            long courseId = inv.getArgument(1);
            return storage.values().stream().anyMatch(c ->
                    c.getUser().getId().equals(userId) && c.getCourse().getId().equals(courseId));
        });
        when(repository.findByUserIdOrderByIssuedAtDesc(anyLong())).thenAnswer(inv -> {
            long userId = inv.getArgument(0);
            List<Certificate> list = new ArrayList<>();
            storage.values().forEach(c -> {
                if (c.getUser().getId().equals(userId)) list.add(c);
            });
            list.sort((a, b) -> b.getIssuedAt().compareTo(a.getIssuedAt()));
            return list;
        });
        when(repository.findByVerificationCode(any(UUID.class))).thenAnswer(inv -> {
            UUID code = inv.getArgument(0);
            return storage.values().stream()
                    .filter(c -> code.equals(c.getVerificationCode()))
                    .findFirst();
        });
    }

    @Test
    void noEmiteSiElCursoNoGeneraCertificado() {
        Course course = newCourse(10L, "Introducción a Python", false);

        Optional<Certificate> result = service.issueIfEligible(user, course);

        assertTrue(result.isEmpty());
        assertTrue(storage.isEmpty(), "no debe persistir nada");
    }

    @Test
    void emiteSiElCursoGeneraCertificado() {
        Course course = newCourse(11L, "Backend con Spring", true);

        Optional<Certificate> result = service.issueIfEligible(user, course);

        assertTrue(result.isPresent());
        Certificate certificate = result.get();
        assertSame(user, certificate.getUser());
        assertSame(course, certificate.getCourse());
        assertNotNull(certificate.getVerificationCode(), "el UUID se asigna al persistir");
        assertNotNull(certificate.getIssuedAt());
        assertEquals(1, storage.size());
    }

    @Test
    void laEmisionEsIdempotente() {
        Course course = newCourse(12L, "Angular esencial", true);

        Certificate primero = service.issueIfEligible(user, course).orElseThrow();
        Certificate segundo = service.issueIfEligible(user, course).orElseThrow();

        assertSame(primero, segundo, "una segunda invocación devuelve el existente");
        assertEquals(1, storage.size(), "no persiste un duplicado");
    }

    @Test
    void listarDevuelveCertificadosDelUsuarioEnOrdenDescendente() {
        Course c1 = newCourse(20L, "Curso A", true);
        Course c2 = newCourse(21L, "Curso B", true);
        Certificate primero = service.issueIfEligible(user, c1).orElseThrow();
        Certificate segundo = service.issueIfEligible(user, c2).orElseThrow();
        primero.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC).minusHours(1));
        segundo.setIssuedAt(OffsetDateTime.now(ZoneOffset.UTC));

        List<CertificateResponse> list = service.listForUser(user.getId());

        assertEquals(2, list.size());
        assertEquals(segundo.getId(), list.get(0).id(), "más reciente primero");
        assertEquals("Ana Pérez", list.get(0).recipientName());
    }

    @Test
    void verificarPorCodigoDevuelveDatosPublicos() {
        Course course = newCourse(30L, "DevOps 101", true);
        Certificate certificate = service.issueIfEligible(user, course).orElseThrow();

        CertificateVerificationResponse response = service.verify(certificate.getVerificationCode());

        assertTrue(response.valid());
        assertEquals("DevOps 101", response.courseName());
        assertEquals("Ana Pérez", response.recipientName());
        assertEquals(certificate.getVerificationCode(), response.verificationCode());
    }

    @Test
    void verificarConCodigoDesconocidoDevuelve404() {
        when(repository.findByVerificationCode(any(UUID.class))).thenReturn(Optional.empty());
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.verify(UUID.randomUUID()));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void renderPdfIncluyeUrlDeVerificacionYUsaBytesDelRenderer() {
        Course course = newCourse(40L, "Bases de datos", true);
        Certificate certificate = service.issueIfEligible(user, course).orElseThrow();

        CertificateService.GeneratedPdf pdf = service.renderPdf(certificate.getVerificationCode());

        assertArrayEquals(new byte[]{1, 2, 3}, pdf.bytes(), "usa los bytes del renderer");
        assertTrue(pdf.filename().startsWith("certificado-") && pdf.filename().endsWith(".pdf"));
        assertEquals("Ana Pérez", renderer.lastRecipient);
        assertEquals("http://localhost:4200/verify/" + certificate.getVerificationCode(),
                renderer.lastUrl);
    }

    @Test
    void nombreDeReceptorUsaUsernameCuandoNoHayNombreCompleto() {
        User anon = newUser(2L, null, null, "guest");
        Course course = newCourse(50L, "Curso X", true);
        Certificate certificate = service.issueIfEligible(anon, course).orElseThrow();

        CertificateVerificationResponse response = service.verify(certificate.getVerificationCode());

        assertEquals("guest", response.recipientName());
    }

    @Test
    void listarDevuelveVacioParaUsuarioSinCertificados() {
        when(repository.findByUserIdOrderByIssuedAtDesc(99L)).thenReturn(Collections.emptyList());
        List<CertificateResponse> list = service.listForUser(99L);
        assertTrue(list.isEmpty());
    }

    // ------------------------------------------------------------------
    // Utilidades para construir entidades de prueba sin infraestructura JPA
    // ------------------------------------------------------------------

    private static User newUser(Long id, String first, String last, String username) {
        User u = new User();
        setField(u, "id", id);
        u.setUsername(username);
        u.setFirstName(first);
        u.setLastName(last);
        u.setEmail(username + "@test.local");
        return u;
    }

    private static Course newCourse(Long id, String name, boolean generatesCertificate) {
        Course c = new Course();
        setField(c, "id", id);
        c.setName(name);
        c.setGeneratesCertificate(generatesCertificate);
        return c;
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field field = findField(target.getClass(), name);
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("No se pudo asignar el campo " + name, e);
        }
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        Class<?> current = type;
        while (current != null) {
            try {
                return current.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    /**
     * Renderer de PDF en memoria que registra la última invocación y
     * devuelve bytes deterministas para validar la interacción con el
     * servicio.
     */
    private static class RecordingPdfRenderer extends CertificatePdfRenderer {
        String lastRecipient;
        String lastUrl;

        @Override
        public byte[] render(Certificate certificate, String verificationUrl, String recipientName) {
            this.lastRecipient = recipientName;
            this.lastUrl = verificationUrl;
            return new byte[]{1, 2, 3};
        }
    }
}
