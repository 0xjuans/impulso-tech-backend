package tech.impulso.notifications.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.notifications.entity.Notification;

import java.time.OffsetDateTime;

/**
 * Repositorio de persistencia para {@link Notification}.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Devuelve las notificaciones del usuario aplicando el filtro
     * opcional de leídas/no leídas.
     *
     * @param userId     identificador del usuario.
     * @param onlyUnread cuando es {@code true} sólo devuelve las no leídas.
     * @param pageable   configuración de paginación y ordenamiento.
     * @return página con las notificaciones coincidentes.
     */
    @Query("""
            select n from Notification n
            where n.user.id = :userId
              and (:onlyUnread = false or n.readAt is null)
            """)
    Page<Notification> findForUser(@Param("userId") Long userId,
                                   @Param("onlyUnread") boolean onlyUnread,
                                   Pageable pageable);

    /**
     * Cuenta las notificaciones no leídas de un usuario.
     *
     * @param userId identificador del usuario.
     * @return cantidad de notificaciones sin leer.
     */
    @Query("select count(n) from Notification n where n.user.id = :userId and n.readAt is null")
    long countUnread(@Param("userId") Long userId);

    /**
     * Marca todas las notificaciones no leídas del usuario como leídas.
     *
     * @param userId identificador del usuario.
     * @param readAt momento a registrar como fecha de lectura.
     * @return cantidad de registros actualizados.
     */
    @Modifying
    @Query("""
            update Notification n
               set n.readAt = :readAt
             where n.user.id = :userId
               and n.readAt is null
            """)
    int markAllAsRead(@Param("userId") Long userId,
                      @Param("readAt") OffsetDateTime readAt);
}
