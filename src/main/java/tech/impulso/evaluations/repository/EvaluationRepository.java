package tech.impulso.evaluations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.evaluations.entity.Evaluation;

import java.util.List;

/**
 * Repositorio de persistencia para {@link Evaluation}.
 */
@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    /**
     * Devuelve las evaluaciones de una lección ordenadas por su
     * posición. Cuando {@code status} es {@code null} no se aplica el
     * filtro correspondiente.
     *
     * @param lessonId identificador de la lección.
     * @param status   estado opcional al que restringir los resultados.
     * @return evaluaciones coincidentes ordenadas.
     */
    @Query("""
            select e from Evaluation e
            where e.lesson.id = :lessonId
              and (:status is null or e.status = :status)
            order by e.orderIndex asc
            """)
    List<Evaluation> findByLesson(@Param("lessonId") Long lessonId,
                                  @Param("status") ContentStatus status);

    /**
     * Devuelve la posición más alta ocupada dentro de una lección. Se
     * utiliza al crear una nueva evaluación para asignarle la siguiente
     * posición disponible.
     *
     * @param lessonId identificador de la lección.
     * @return posición más alta o {@code null} si aún no hay evaluaciones.
     */
    @Query("select max(e.orderIndex) from Evaluation e where e.lesson.id = :lessonId")
    Integer findMaxOrderIndex(@Param("lessonId") Long lessonId);
}
