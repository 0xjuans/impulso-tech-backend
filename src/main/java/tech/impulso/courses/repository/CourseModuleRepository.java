package tech.impulso.courses.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.courses.entity.CourseModule;

import java.util.List;

/**
 * Repositorio de persistencia para {@link CourseModule}.
 */
@Repository
public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {

    /**
     * Devuelve los módulos de un curso ordenados por su posición dentro
     * del curso. Cuando {@code status} es {@code null} no se aplica el
     * filtro correspondiente.
     *
     * @param courseId identificador del curso.
     * @param status   estado opcional al que restringir los resultados.
     * @return módulos coincidentes ordenados por posición.
     */
    @Query("""
            select m from CourseModule m
            where m.course.id = :courseId
              and (:status is null or m.status = :status)
            order by m.orderIndex asc
            """)
    List<CourseModule> findByCourse(@Param("courseId") Long courseId,
                                    @Param("status") ContentStatus status);

    /**
     * Devuelve la posición más alta ocupada dentro de un curso. Se
     * utiliza al crear un nuevo módulo para asignarle la siguiente
     * posición disponible.
     *
     * @param courseId identificador del curso.
     * @return posición más alta o {@code null} si aún no hay módulos.
     */
    @Query("select max(m.orderIndex) from CourseModule m where m.course.id = :courseId")
    Integer findMaxOrderIndex(@Param("courseId") Long courseId);
}
