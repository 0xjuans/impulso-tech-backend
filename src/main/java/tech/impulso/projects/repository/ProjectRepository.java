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
            where (cast(:search as string) is null or
                   lower(p.name)        like lower(concat('%', cast(:search as string), '%')) or
                   lower(p.description) like lower(concat('%', cast(:search as string), '%')))
              and p.difficulty = coalesce(:difficulty, p.difficulty)
              and p.status = coalesce(:status, p.status)
              and p.learningRoute.id = coalesce(:learningRouteId, p.learningRoute.id)
              and p.course.id = coalesce(:courseId, p.course.id)
              and p.module.id = coalesce(:moduleId, p.module.id)
            """)
    Page<Project> search(@Param("search") String search,
                         @Param("difficulty") DifficultyLevel difficulty,
                         @Param("status") ContentStatus status,
                         @Param("learningRouteId") Long learningRouteId,
                         @Param("courseId") Long courseId,
                         @Param("moduleId") Long moduleId,
                         Pageable pageable);
}
