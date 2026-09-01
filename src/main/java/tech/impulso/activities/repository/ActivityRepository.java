package tech.impulso.activities.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.activities.entity.Activity;
import tech.impulso.common.content.ContentStatus;

import java.util.List;

/**
 * Repositorio de persistencia para {@link Activity}.
 */
@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    /**
     * Devuelve las actividades de una lección ordenadas por su posición.
     * Cuando {@code status} es {@code null} no se aplica el filtro.
     *
     * @param lessonId identificador de la lección.
     * @param status   estado opcional al que restringir los resultados.
     * @return actividades coincidentes ordenadas.
     */
    @Query("""
            select a from Activity a
            where a.lesson.id = :lessonId
              and (:status is null or a.status = :status)
            order by a.orderIndex asc
            """)
    List<Activity> findByLesson(@Param("lessonId") Long lessonId,
                                @Param("status") ContentStatus status);

    /**
     * Devuelve la posición más alta ocupada dentro de una lección. Se
     * utiliza al crear una nueva actividad para asignarle la siguiente
     * posición disponible.
     *
     * @param lessonId identificador de la lección.
     * @return posición más alta o {@code null} si aún no hay actividades.
     */
    @Query("select max(a.orderIndex) from Activity a where a.lesson.id = :lessonId")
    Integer findMaxOrderIndex(@Param("lessonId") Long lessonId);
}
