package tech.impulso.challenges.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.challenges.entity.Challenge;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;

/**
 * Repositorio de persistencia para {@link Challenge}.
 */
@Repository
public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    /**
     * Búsqueda paginada de retos aplicando filtros opcionales.
     *
     * @param search          fragmento a buscar en nombre o descripción.
     * @param difficulty      nivel de dificultad opcional.
     * @param status          estado opcional.
     * @param language        lenguaje permitido (búsqueda dentro del CSV).
     * @param learningRouteId ruta asociada opcional.
     * @param courseId        curso asociado opcional.
     * @param moduleId        módulo asociado opcional.
     * @param lessonId        lección asociada opcional.
     * @param pageable        configuración de paginación y ordenamiento.
     * @return página con los retos coincidentes.
     */
    @Query("""
            select c from Challenge c
            where (:search          is null or
                   lower(c.name)        like lower(concat('%', :search, '%')) or
                   lower(c.description) like lower(concat('%', :search, '%')))
              and (:difficulty      is null or c.difficulty = :difficulty)
              and (:status          is null or c.status     = :status)
              and (:language        is null or lower(c.allowedLanguages) like lower(concat('%', :language, '%')))
              and (:learningRouteId is null or c.learningRoute.id = :learningRouteId)
              and (:courseId        is null or c.course.id        = :courseId)
              and (:moduleId        is null or c.module.id        = :moduleId)
              and (:lessonId        is null or c.lesson.id        = :lessonId)
            """)
    Page<Challenge> search(@Param("search") String search,
                           @Param("difficulty") DifficultyLevel difficulty,
                           @Param("status") ContentStatus status,
                           @Param("language") String language,
                           @Param("learningRouteId") Long learningRouteId,
                           @Param("courseId") Long courseId,
                           @Param("moduleId") Long moduleId,
                           @Param("lessonId") Long lessonId,
                           Pageable pageable);
}
