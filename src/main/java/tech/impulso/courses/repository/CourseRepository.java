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

    /**
     * Búsqueda global de cursos por coincidencia de texto en nombre o
     * descripción, respetando la visibilidad del solicitante (RF-035).
     *
     * <p>Cuando {@code ownerId} es nulo se aceptan resultados cuyo estado
     * sea {@code PUBLICADO}; si adicionalmente {@code includeAll} es
     * {@code true} no se aplica el filtro de estado (uso administrativo).
     * Cuando {@code ownerId} está presente también se incluyen los cursos
     * cuyo instructor sea el usuario indicado, independientemente del
     * estado.</p>
     *
     * @param q          fragmento a buscar (nunca nulo, ya normalizado).
     * @param ownerId    identificador del instructor que solicita, o nulo.
     * @param includeAll indica si se deben incluir cursos en cualquier estado.
     * @param pageable   configuración de paginación.
     * @return página con los cursos coincidentes.
     */
    @Query("""
            select c from Course c
            where (lower(c.name)        like lower(concat('%', :q, '%')) or
                   lower(c.description) like lower(concat('%', :q, '%')))
              and (
                    :includeAll = true
                 or c.status = tech.impulso.common.content.ContentStatus.PUBLICADO
                 or (:ownerId is not null and c.instructor.id = :ownerId)
              )
            """)
    Page<Course> globalSearch(@Param("q") String q,
                              @Param("ownerId") Long ownerId,
                              @Param("includeAll") boolean includeAll,
                              Pageable pageable);

    /**
     * Cuenta cuántos cursos existen en el estado indicado. Se utiliza en
     * el panel del administrador (RF-033).
     */
    long countByStatus(ContentStatus status);

    /**
     * Cuenta cuántos cursos gestiona el instructor indicado en el estado
     * suministrado. Se utiliza en el panel del instructor (RF-032).
     */
    long countByInstructorIdAndStatus(Long instructorId, ContentStatus status);

    /**
     * Cuenta el total de cursos gestionados por el instructor indicado
     * sin filtrar por estado.
     */
    long countByInstructorId(Long instructorId);

    /**
     * Cuenta cuántos cursos existen para el nivel de dificultad indicado.
     * Se utiliza para la distribución de cursos por dificultad (RF-058).
     */
    long countByDifficulty(DifficultyLevel difficulty);
}
