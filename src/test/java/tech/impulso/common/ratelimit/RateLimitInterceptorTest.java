package tech.impulso.common.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del {@link RateLimitInterceptor}.
 *
 * <p>Cubre la ruta feliz (pasa la petición y añade las cabeceras de
 * telemetría), la ruta bloqueada (responde 429 con {@code Retry-After}
 * y {@link jakarta.servlet.http.HttpServletResponse#setContentType}),
 * el corto-circuito cuando no hay anotación y el respeto al toggle
 * global desde {@link RateLimitProperties}.</p>
 */
class RateLimitInterceptorTest {

    private RateLimitService service;
    private RateLimitProperties properties;
    private RateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() {
        service = mock(RateLimitService.class);
        properties = new RateLimitProperties();
        interceptor = new RateLimitInterceptor(service, properties);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void permiteYExponeCabecerasDeTelemetria() throws Exception {
        HandlerMethod handler = handler("rateLimited");
        when(service.check(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new RateLimitService.Decision(true, 4, 42));
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(req, res, handler);

        assertTrue(allowed);
        assertEquals("3", res.getHeader("X-RateLimit-Limit"));
        assertEquals("4", res.getHeader("X-RateLimit-Remaining"));
        assertEquals("42", res.getHeader("X-RateLimit-Reset"));
    }

    @Test
    void bloqueaConHttp429YRetryAfter() throws Exception {
        HandlerMethod handler = handler("rateLimited");
        when(service.check(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new RateLimitService.Decision(false, 0, 17));
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(req, res, handler);

        assertFalse(allowed);
        assertEquals(429, res.getStatus());
        assertEquals("17", res.getHeader("Retry-After"));
        assertNotNull(res.getContentType());
        assertTrue(res.getContentType().contains("application/json"));
        assertTrue(res.getContentAsString().contains("\"status\":429"));
    }

    @Test
    void ignoraHandlersSinAnotacion() throws Exception {
        HandlerMethod handler = handler("noLimit");
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/y");
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(req, res, handler);

        assertTrue(allowed);
        verify(service, times(0)).check(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void toggleGlobalDesactivaElLimite() throws Exception {
        properties.setEnabled(false);
        HandlerMethod handler = handler("rateLimited");
        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/x");
        MockHttpServletResponse res = new MockHttpServletResponse();

        boolean allowed = interceptor.preHandle(req, res, handler);

        assertTrue(allowed);
        verify(service, times(0)).check(anyString(), anyString(), anyInt(), anyInt());
    }

    @Test
    void usaUsuarioAutenticadoComoClave() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ana@test.local", "n/a", List.of()));
        HandlerMethod handler = handler("rateLimited");
        when(service.check(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new RateLimitService.Decision(true, 2, 30));

        interceptor.preHandle(new MockHttpServletRequest(), new MockHttpServletResponse(), handler);

        verify(service).check(
                org.mockito.ArgumentMatchers.eq("auth-login"),
                org.mockito.ArgumentMatchers.eq("u:ana@test.local"),
                anyInt(),
                anyInt()
        );
    }

    @Test
    void cuandoNoHayAutenticacionUsaIpDelXForwardedFor() throws Exception {
        HandlerMethod handler = handler("rateLimited");
        when(service.check(anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(new RateLimitService.Decision(true, 2, 30));
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.addHeader("X-Forwarded-For", "203.0.113.42, 10.0.0.1");

        interceptor.preHandle(req, new MockHttpServletResponse(), handler);

        verify(service).check(
                org.mockito.ArgumentMatchers.eq("auth-login"),
                org.mockito.ArgumentMatchers.eq("ip:203.0.113.42"),
                anyInt(),
                anyInt()
        );
    }

    // ----- Handlers de prueba -------------------------------------------

    private HandlerMethod handler(String methodName) throws NoSuchMethodException {
        Method method = SampleController.class.getDeclaredMethod(methodName);
        return new HandlerMethod(new SampleController(), method);
    }

    static class SampleController {
        @RateLimit(bucket = "auth-login", limit = 3, windowSeconds = 60)
        public void rateLimited() { }

        public void noLimit() { }
    }
}
