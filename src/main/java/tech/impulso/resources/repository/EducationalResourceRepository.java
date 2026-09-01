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
            where (:search          is null or
                   lower(r.name)        like lower(concat('%', :search, '%')) or
                   lower(r.description) like lower(concat('%', :search, '%')) or
                   lower(r.topic)       like lower(concat('%', :search, '%')) or
                   lower(r.author)      like lower(concat('%', :search, '%')))
              and (:type             is null or r.type       = :type)
              and (:difficulty       is null or r.difficulty = :difficulty)
              and (:status           is null or r.status     = :status)
              and (:learningRouteId  is null or r.learningRoute.id = :learningRouteId)
              and (:courseId         is null or r.course.id        = :courseId)
              and (:moduleId         is null or r.module.id        = :moduleId)
              and (:lessonId         is null or r.lesson.id        = :lessonId)
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
