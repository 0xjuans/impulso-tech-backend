package tech.impulso.learningroutes.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.content.DifficultyLevel;
import tech.impulso.learningroutes.entity.LearningRoute;

/**
 * Repositorio de persistencia para {@link LearningRoute}.
 */
@Repository
public interface LearningRouteRepository extends JpaRepository<LearningRoute, Long> {

    /**
     * Búsqueda paginada de rutas de aprendizaje aplicando filtros
     * opcionales.
     *
     * @param search     fragmento a buscar en nombre o descripción.
     * @param difficulty nivel de dificultad opcional.
     * @param status     estado opcional. Cuando la petición proviene de un
     *                   estudiante siempre debe filtrarse por
     *                   {@link ContentStatus#PUBLICADO}.
     * @param pageable   configuración de paginación y ordenamiento.
     * @return página con las rutas coincidentes.
     */
    @Query("""
            select r from LearningRoute r
            where (:search     is null or
                   lower(r.name)        like lower(concat('%', :search, '%')) or
                   lower(r.description) like lower(concat('%', :search, '%')))
              and (:difficulty is null or r.difficulty = :difficulty)
              and (:status     is null or r.status     = :status)
            """)
    Page<LearningRoute> search(@Param("search") String search,
                               @Param("difficulty") DifficultyLevel difficulty,
                               @Param("status") ContentStatus status,
                               Pageable pageable);

    /**
     * Búsqueda global de rutas de aprendizaje por coincidencia de texto
     * en nombre o descripción, respetando la visibilidad del solicitante
     * (RF-035).
     *
     * @param q          fragmento a buscar (nunca nulo, ya normalizado).
     * @param ownerId    identificador del instructor solicitante, o nulo.
     * @param includeAll indica si se deben incluir rutas en cualquier estado.
     * @param pageable   configuración de paginación.
     * @return página con las rutas coincidentes.
     */
    @Query("""
            select r from LearningRoute r
            where (lower(r.name)        like lower(concat('%', :q, '%')) or
                   lower(r.description) like lower(concat('%', :q, '%')))
              and (
                    :includeAll = true
                 or r.status = tech.impulso.common.content.ContentStatus.PUBLICADO
                 or (:ownerId is not null and r.instructor.id = :ownerId)
              )
            """)
    Page<LearningRoute> globalSearch(@Param("q") String q,
                                     @Param("ownerId") Long ownerId,
                                     @Param("includeAll") boolean includeAll,
                                     Pageable pageable);

    /**
     * Cuenta cuántas rutas existen en el estado indicado. Se utiliza en
     * el panel del administrador (RF-033).
     */
    long countByStatus(ContentStatus status);

    /**
     * Cuenta cuántas rutas gestiona el instructor indicado en el estado
     * suministrado. Se utiliza en el panel del instructor (RF-032).
     */
    long countByInstructorIdAndStatus(Long instructorId, ContentStatus status);

    /**
     * Cuenta el total de rutas gestionadas por el instructor sin filtrar
     * por estado.
     */
    long countByInstructorId(Long instructorId);
}
