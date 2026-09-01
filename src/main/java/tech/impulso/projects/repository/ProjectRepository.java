package tech.impulso.projects.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.projects.entity.Project;

/**
 * Repositorio de persistencia para {@link Project}.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * Búsqueda paginada de proyectos aplicando filtros opcionales.
     *
     * @param search          fragmento a buscar en nombre o descripción.
     * @param difficulty      nivel de dificultad opcional.
     * @param status          estado opcional.
     * @param learningRouteId ruta asociada opcional.
     * @param courseId        curso asociado opcional.
     * @param moduleId        módulo asociado opcional.
     * @param pageable        configuración de paginación y ordenamiento.
     * @return página con los proyectos coincidentes.
     */
    @Query("""
            select p from Project p
            where (:search          is null or
                   lower(p.name)        like lower(concat('%', :search, '%')) or
                   lower(p.description) like lower(concat('%', :search, '%')))
              and (:difficulty      is null or p.difficulty = :difficulty)
              and (:status          is null or p.status     = :status)
              and (:learningRouteId is null or p.learningRoute.id = :learningRouteId)
              and (:courseId        is null or p.course.id        = :courseId)
              and (:moduleId        is null or p.module.id        = :moduleId)
            """)
    Page<Project> search(@Param("search") String search,
                         @Param("difficulty") DifficultyLevel difficulty,
                         @Param("status") ContentStatus status,
                         @Param("learningRouteId") Long learningRouteId,
                         @Param("courseId") Long courseId,
                         @Param("moduleId") Long moduleId,
                         Pageable pageable);
}
