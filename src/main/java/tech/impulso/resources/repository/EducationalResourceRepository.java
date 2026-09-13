package tech.impulso.resources.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.resources.entity.EducationalResource;
import tech.impulso.resources.entity.ResourceType;

/**
 * Repositorio de persistencia para {@link EducationalResource}.
 */
@Repository
public interface EducationalResourceRepository extends JpaRepository<EducationalResource, Long> {

    /**
     * Búsqueda paginada de recursos aplicando filtros opcionales.
     *
     * @param search          fragmento a buscar en nombre, descripción, tema o autor.
     * @param type            tipo del recurso, opcional.
     * @param difficulty      nivel de dificultad, opcional.
     * @param status          estado del ciclo de vida, opcional.
     * @param learningRouteId ruta asociada, opcional.
     * @param courseId        curso asociado, opcional.
     * @param moduleId        módulo asociado, opcional.
     * @param lessonId        lección asociada, opcional.
     * @param pageable        configuración de paginación y ordenamiento.
     * @return página con los recursos coincidentes.
     */
    @Query("""
            select r from EducationalResource r
            where (cast(:search as string) is null or
                   lower(r.name)        like lower(concat('%', cast(:search as string), '%')) or
                   lower(r.description) like lower(concat('%', cast(:search as string), '%')) or
                   lower(r.topic)       like lower(concat('%', cast(:search as string), '%')) or
                   lower(r.author)      like lower(concat('%', cast(:search as string), '%')))
              and r.type = coalesce(:type, r.type)
              and r.difficulty = coalesce(:difficulty, r.difficulty)
              and r.status = coalesce(:status, r.status)
              and r.learningRoute.id = coalesce(:learningRouteId, r.learningRoute.id)
              and r.course.id = coalesce(:courseId, r.course.id)
              and r.module.id = coalesce(:moduleId, r.module.id)
              and r.lesson.id = coalesce(:lessonId, r.lesson.id)
            """)
    Page<EducationalResource> search(@Param("search") String search,
                                     @Param("type") ResourceType type,
                                     @Param("difficulty") DifficultyLevel difficulty,
                                     @Param("status") ContentStatus status,
                                     @Param("learningRouteId") Long learningRouteId,
                                     @Param("courseId") Long courseId,
                                     @Param("moduleId") Long moduleId,
                                     @Param("lessonId") Long lessonId,
                                     Pageable pageable);
}
