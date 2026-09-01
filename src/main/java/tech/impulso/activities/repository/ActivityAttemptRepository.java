package tech.impulso.activities.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.activities.entity.ActivityAttempt;

import java.util.List;

/**
 * Repositorio de persistencia para {@link ActivityAttempt}.
 */
@Repository
public interface ActivityAttemptRepository extends JpaRepository<ActivityAttempt, Long> {

    /**
     * Cuenta cuántos intentos ha realizado un usuario sobre una
     * actividad. Se utiliza para validar el límite de intentos
     * configurado.
     *
     * @param userId     identificador del usuario.
     * @param activityId identificador de la actividad.
     * @return cantidad total de intentos realizados.
     */
    long countByUserIdAndActivityId(Long userId, Long activityId);

    /**
     * Indica si el usuario ya tiene al menos un intento correcto sobre
     * la actividad. Se utiliza para determinar si se debe otorgar la
     * recompensa por primera vez.
     *
     * @param userId     identificador del usuario.
     * @param activityId identificador de la actividad.
     * @return {@code true} cuando ya existe un intento correcto previo.
     */
    @Query("""
            select case when count(a) > 0 then true else false end
            from ActivityAttempt a
            where a.user.id = :userId
              and a.activity.id = :activityId
              and a.correct = true
            """)
    boolean hasCorrectAttempt(@Param("userId") Long userId,
                              @Param("activityId") Long activityId);

    /**
     * Devuelve el historial de intentos de un usuario en una actividad,
     * ordenado por fecha descendente.
     *
     * @param userId     identificador del usuario.
     * @param activityId identificador de la actividad.
     * @return lista de intentos.
     */
    List<ActivityAttempt> findByUserIdAndActivityIdOrderByCreatedAtDesc(Long userId, Long activityId);
}
