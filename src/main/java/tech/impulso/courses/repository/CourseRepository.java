package tech.impulso.courses.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.courses.entity.Course;

/**
 * Repositorio de persistencia para {@link Course}.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Búsqueda paginada de cursos con filtros opcionales.
     *
     * @param search          fragmento a buscar en nombre o descripción.
     * @param difficulty      nivel de dificultad opcional.
     * @param status          estado opcional.
     * @param learningRouteId identificador de la ruta a la que pertenece
     *                        el curso; cuando es nulo no se aplica el filtro.
     * @param pageable        configuración de paginación y ordenamiento.
     * @return página con los cursos coincidentes.
     */
    @Query("""
            select c from Course c
            where (:search          is null or
                   lower(c.name)        like lower(concat('%', :search, '%')) or
                   lower(c.description) like lower(concat('%', :search, '%')))
              and (:difficulty      is null or c.difficulty = :difficulty)
              and (:status          is null or c.status     = :status)
              and (:learningRouteId is null or c.learningRoute.id = :learningRouteId)
            """)
    Page<Course> search(@Param("search") String search,
                        @Param("difficulty") DifficultyLevel difficulty,
                        @Param("status") ContentStatus status,
                        @Param("learningRouteId") Long learningRouteId,
                        Pageable pageable);
}
