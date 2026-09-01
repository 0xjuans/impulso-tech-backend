package tech.impulso.lessons.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.lessons.entity.Lesson;

import java.util.List;

/**
 * Repositorio de persistencia para {@link Lesson}.
 */
@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    /**
     * Devuelve las lecciones de un módulo ordenadas por su posición.
     * Cuando {@code status} es {@code null} no se aplica el filtro
     * correspondiente.
     *
     * @param moduleId identificador del módulo.
     * @param status   estado opcional al que restringir los resultados.
     * @return lecciones coincidentes ordenadas por posición.
     */
    @Query("""
            select l from Lesson l
            where l.module.id = :moduleId
              and (:status is null or l.status = :status)
            order by l.orderIndex asc
            """)
    List<Lesson> findByModule(@Param("moduleId") Long moduleId,
                              @Param("status") ContentStatus status);

    /**
     * Devuelve la posición más alta ocupada dentro de un módulo. Se
     * utiliza al crear una nueva lección para asignarle la siguiente
     * posición disponible.
     *
     * @param moduleId identificador del módulo.
     * @return posición más alta o {@code null} si aún no hay lecciones.
     */
    @Query("select max(l.orderIndex) from Lesson l where l.module.id = :moduleId")
    Integer findMaxOrderIndex(@Param("moduleId") Long moduleId);

    /**
     * Cuenta las lecciones obligatorias publicadas de un curso, teniendo
     * en cuenta tanto el estado de la lección como el estado del módulo
     * al que pertenece. Sólo estas lecciones se consideran para calcular
     * la finalización del curso (RF-011).
     *
     * @param courseId identificador del curso.
     * @return cantidad de lecciones obligatorias publicadas.
     */
    @Query("""
            select count(l) from Lesson l
            where l.module.course.id = :courseId
              and l.optional = false
              and l.status = tech.impulso.common.content.ContentStatus.PUBLICADO
              and l.module.status = tech.impulso.common.content.ContentStatus.PUBLICADO
            """)
    long countMandatoryPublishedInCourse(@Param("courseId") Long courseId);
}
