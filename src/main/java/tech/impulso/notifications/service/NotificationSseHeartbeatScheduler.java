package tech.impulso.notifications.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler que envía un comentario keep-alive a todas las conexiones
 * SSE abiertas.
 *
 * <p>Muchos proxies intermedios (por ejemplo balanceadores) cierran las
 * conexiones inactivas después de 30-60 segundos. Enviar un heartbeat
 * cada 20 segundos garantiza que las suscripciones sigan vivas mientras
 * el usuario mantenga la plataforma abierta.</p>
 */
@Component
public class NotificationSseHeartbeatScheduler {

    private final NotificationSseBroadcaster broadcaster;

    public NotificationSseHeartbeatScheduler(NotificationSseBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    /**
     * Envía el heartbeat cada 20 segundos a todas las conexiones activas.
     */
    @Scheduled(fixedDelay = 20_000L)
    public void tick() {
        broadcaster.heartbeat();
    }
}
