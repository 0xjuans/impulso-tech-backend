package tech.impulso.notifications.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import tech.impulso.notifications.dto.NotificationResponse;

/**
 * Coordinador de emisores {@link SseEmitter} para la entrega de
 * notificaciones en tiempo real (RF-026).
 *
 * <p>Mantiene, por cada usuario, la lista de conexiones SSE activas
 * (por ejemplo cuando el usuario tiene la plataforma abierta en varias
 * pestañas). Cuando el {@link NotificationService} genera una nueva
 * notificación, este componente empuja el evento a todas las conexiones
 * abiertas del destinatario.</p>
 *
 * <p>Las conexiones se limpian automáticamente al completarse, agotarse
 * su tiempo máximo o producirse un error de escritura, evitando fugas
 * de recursos en el servidor.</p>
 */
@Component
public class NotificationSseBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(NotificationSseBroadcaster.class);

    /**
     * Tiempo máximo de vida de una conexión SSE antes de que el cliente
     * deba volver a conectarse. Se establece en 30 minutos para
     * equilibrar frescura y consumo de recursos.
     */
    private static final long DEFAULT_TIMEOUT_MS = 30L * 60L * 1000L;

    /** Nombre del evento SSE que representa una notificación nueva. */
    public static final String EVENT_NOTIFICATION = "notification";

    /** Nombre del evento SSE de saludo tras conectar. */
    public static final String EVENT_HELLO = "hello";

    /** Nombre del evento SSE para el heartbeat de mantenimiento. */
    public static final String EVENT_HEARTBEAT = "heartbeat";

    private final Map<Long, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    /**
     * Registra un nuevo suscriptor para el usuario indicado.
     *
     * @param userId identificador del usuario que abre la conexión.
     * @return el {@link SseEmitter} que el controlador debe devolver a
     *         la petición HTTP.
     */
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        List<SseEmitter> list = emittersByUser.computeIfAbsent(userId, id -> new CopyOnWriteArrayList<>());
        list.add(emitter);

        emitter.onCompletion(() -> remove(userId, emitter));
        emitter.onTimeout(() -> {
            emitter.complete();
            remove(userId, emitter);
        });
        emitter.onError(e -> remove(userId, emitter));

        try {
            emitter.send(SseEmitter.event()
                    .name(EVENT_HELLO)
                    .data(Map.of("ok", true)));
        } catch (IOException e) {
            remove(userId, emitter);
        }
        return emitter;
    }

    /**
     * Empuja una notificación al usuario indicado a través de todas las
     * conexiones SSE activas que mantenga.
     *
     * @param userId       destinatario de la notificación.
     * @param notification notificación a entregar.
     */
    public void broadcast(Long userId, NotificationResponse notification) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(EVENT_NOTIFICATION)
                        .data(notification));
            } catch (Exception e) {
                log.debug("Se cerró un emisor SSE por error al enviar notificación al usuario {}", userId);
                remove(userId, emitter);
                try {
                    emitter.complete();
                } catch (Exception ignored) {
                    // El emisor ya podría estar cerrado; no se propaga.
                }
            }
        }
    }

    /**
     * Envía un heartbeat a todos los suscriptores para evitar el cierre
     * de conexiones inactivas por proxies intermedios. Debe invocarse de
     * forma periódica desde un scheduler.
     */
    public void heartbeat() {
        emittersByUser.forEach((userId, emitters) -> {
            for (SseEmitter emitter : emitters) {
                try {
                    emitter.send(SseEmitter.event()
                            .name(EVENT_HEARTBEAT)
                            .comment("ping"));
                } catch (Exception e) {
                    remove(userId, emitter);
                    try {
                        emitter.complete();
                    } catch (Exception ignored) {
                        // Sin acción: el emisor puede estar cerrado.
                    }
                }
            }
        });
    }

    /**
     * Devuelve la cantidad de conexiones SSE activas de un usuario. De
     * utilidad principalmente para pruebas y para diagnóstico.
     *
     * @param userId identificador del usuario.
     * @return número de conexiones actualmente abiertas.
     */
    public int activeConnections(Long userId) {
        List<SseEmitter> emitters = emittersByUser.get(userId);
        return emitters == null ? 0 : emitters.size();
    }

    private void remove(Long userId, SseEmitter emitter) {
        List<SseEmitter> list = emittersByUser.get(userId);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                emittersByUser.remove(userId, list);
            }
        }
    }
}
