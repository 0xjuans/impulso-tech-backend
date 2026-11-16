package tech.impulso.notifications.service;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.impulso.notifications.dto.NotificationResponse;
import tech.impulso.notifications.entity.NotificationType;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pruebas unitarias del {@link NotificationSseBroadcaster}.
 *
 * <p>Verifican la correcta gestión del ciclo de vida de los emisores:
 * cómo se registran al suscribir, cómo se descartan al completarse y
 * cómo se difunden los eventos únicamente a los emisores activos del
 * destinatario correspondiente.</p>
 */
class NotificationSseBroadcasterTest {

    private NotificationSseBroadcaster broadcaster;

    @BeforeEach
    void setUp() {
        broadcaster = new NotificationSseBroadcaster();
    }

    @Test
    void subscribeRegistraEmisorParaElUsuario() {
        broadcaster.subscribe(42L);
        assertEquals(1, broadcaster.activeConnections(42L));
    }

    @Test
    void broadcastAUsuarioSinSuscriptoresNoLanzaError() {
        NotificationResponse payload = samplePayload();
        broadcaster.broadcast(999L, payload);
        assertEquals(0, broadcaster.activeConnections(999L));
    }

    @Test
    void broadcastEnviaEventoAlEmisorActivoDelUsuario() {
        broadcaster.subscribe(7L);
        broadcaster.broadcast(7L, samplePayload());
        // El emisor sigue activo tras un envío exitoso porque no se
        // produjeron errores de escritura sobre el flujo interno.
        assertEquals(1, broadcaster.activeConnections(7L));
    }

    @Test
    void heartbeatMantieneEmisoresActivos() {
        broadcaster.subscribe(3L);
        broadcaster.heartbeat();
        assertEquals(1, broadcaster.activeConnections(3L));
    }

    @Test
    void subscribeSoportaVariasConexionesDelMismoUsuario() {
        broadcaster.subscribe(5L);
        broadcaster.subscribe(5L);
        assertEquals(2, broadcaster.activeConnections(5L));
    }

    private NotificationResponse samplePayload() {
        return new NotificationResponse(
                1L,
                NotificationType.GENERIC,
                "Hola",
                "Mensaje de prueba",
                null,
                null,
                false,
                null,
                OffsetDateTime.now());
    }
}
