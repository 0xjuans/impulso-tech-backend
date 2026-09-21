package tech.impulso.common.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas del {@link RateLimitService}.
 *
 * <p>Se usa un {@link StringRedisTemplate} mockeado con un contador en
 * memoria para simular el comportamiento de {@code INCR} y verificar
 * la decisión emitida ante cada solicitud.</p>
 */
class RateLimitServiceTest {

    private StringRedisTemplate template;
    private ValueOperations<String, String> ops;
    private RateLimitService service;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        template = mock(StringRedisTemplate.class);
        ops = mock(ValueOperations.class);
        when(template.opsForValue()).thenReturn(ops);
        service = new RateLimitService(template);
    }

    @Test
    void permiteHastaAlcanzarElLimite() {
        AtomicLong counter = new AtomicLong();
        when(ops.increment(anyString())).thenAnswer(inv -> counter.incrementAndGet());

        for (int i = 1; i <= 3; i++) {
            RateLimitService.Decision d = service.check("auth-login", "u:1", 3, 60);
            assertTrue(d.allowed(), "solicitud " + i + " debe pasar");
            assertEquals(3 - i, d.remaining());
        }
    }

    @Test
    void rechazaCuandoSeExcedeElLimite() {
        AtomicLong counter = new AtomicLong();
        when(ops.increment(anyString())).thenAnswer(inv -> counter.incrementAndGet());

        for (int i = 0; i < 3; i++) service.check("auth-login", "u:1", 3, 60);
        RateLimitService.Decision blocked = service.check("auth-login", "u:1", 3, 60);

        assertFalse(blocked.allowed(), "la cuarta solicitud debe ser rechazada");
        assertEquals(0L, blocked.remaining());
    }

    @Test
    void aplicaTtlSoloEnLaPrimeraSolicitudDeLaVentana() {
        AtomicLong counter = new AtomicLong();
        when(ops.increment(anyString())).thenAnswer(inv -> counter.incrementAndGet());

        service.check("auth-login", "u:1", 5, 60);
        service.check("auth-login", "u:1", 5, 60);

        verify(template, times(1)).expire(anyString(), any(Duration.class));
    }

    @Test
    void usaClaveDistintaPorBucketYCliente() {
        AtomicLong counter = new AtomicLong();
        when(ops.increment(anyString())).thenAnswer(inv -> counter.incrementAndGet());

        service.check("auth-login", "u:1", 5, 60);
        service.check("auth-login", "u:2", 5, 60);
        service.check("ai-message", "u:1", 5, 60);

        verify(ops, times(3)).increment(anyString());
    }

    @Test
    void falloDeRedisNoBloqueaAlUsuario() {
        when(ops.increment(anyString())).thenThrow(
                new DataAccessResourceFailureException("Redis unavailable"));

        RateLimitService.Decision decision = service.check("auth-login", "u:1", 3, 60);

        assertTrue(decision.allowed(),
                "si Redis falla debemos abrir el flujo en vez de bloquear al usuario");
    }

    @Test
    void resetInSecondsReflejaLosSegundosRestantesDeLaVentana() {
        when(ops.increment(anyString())).thenReturn(1L);

        RateLimitService.Decision decision = service.check("auth-login", "u:1", 5, 60);

        long reset = decision.resetInSeconds();
        assertTrue(reset > 0 && reset <= 60,
                "resetInSeconds debe estar dentro del tamaño de la ventana");
    }
}
