package tech.impulso.labs.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.labs.entity.Lab;

/**
 * Repositorio de persistencia para {@link Lab}.
 */
@Repository
public interface LabRepository extends JpaRepository<Lab, Long> {

    /**
     * Búsqueda paginada de laboratorios con filtros opcionales.
     *
     * @param search      fragmento a buscar en título o descripción.
     * @param language    lenguaje objetivo, opcional.
     * @param status      estado opcional. Los estudiantes siempre deben
     *                    filtrar por {@link ContentStatus#PUBLICADO}.
     * @param instructor  identificador del instructor, opcional.
     * @param courseId    identificador del curso, opcional.
     * @param pageable    configuración de paginación y ordenamiento.
     * @return página con los laboratorios coincidentes.
     */
    @Query("""
            select l from Lab l
            where (:search     is null or
                   lower(l.title)       like lower(concat('%', :search, '%')) or
                   lower(l.description) like lower(concat('%', :search, '%')))
              and (:language   is null or l.language = :language)
              and (:status     is null or l.status   = :status)
              and (:instructor is null or l.instructor.id = :instructor)
              and (:courseId   is null or l.course.id     = :courseId)
            """)
    Page<Lab> search(@Param("search") String search,
                     @Param("language") String language,
                     @Param("status") ContentStatus status,
                     @Param("instructor") Long instructor,
                     @Param("courseId") Long courseId,
                     Pageable pageable);
}
