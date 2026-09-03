package tech.impulso.projects.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.projects.entity.ProjectSubmission;
import tech.impulso.projects.entity.ProjectSubmissionStatus;

import java.util.List;

/**
 * Repositorio de persistencia para {@link ProjectSubmission}.
 */
@Repository
public interface ProjectSubmissionRepository extends JpaRepository<ProjectSubmission, Long> {

    /**
     * Devuelve las entregas de un estudiante sobre un proyecto,
     * ordenadas de la más reciente a la más antigua.
     *
     * @param userId    identificador del estudiante.
     * @param projectId identificador del proyecto.
     * @return entregas del estudiante para ese proyecto.
     */
    List<ProjectSubmission> findByUserIdAndProjectIdOrderBySubmissionNumberDesc(Long userId, Long projectId);

    /**
     * Devuelve las entregas de un proyecto con soporte de filtro por
     * estado. Se utiliza en el panel del instructor.
     *
     * @param projectId identificador del proyecto.
     * @param status    estado opcional al que restringir los resultados.
     * @param pageable  configuración de paginación y ordenamiento.
     * @return página con las entregas coincidentes.
     */
    @Query("""
            select s from ProjectSubmission s
            where s.project.id = :projectId
              and (:status is null or s.status = :status)
            """)
    Page<ProjectSubmission> findByProject(@Param("projectId") Long projectId,
                                          @Param("status") ProjectSubmissionStatus status,
                                          Pageable pageable);

    /**
     * Devuelve el número más alto de entrega registrado para un
     * estudiante sobre un proyecto. Se utiliza al crear una entrega
     * nueva para asignarle el siguiente número.
     *
     * @param userId    identificador del estudiante.
     * @param projectId identificador del proyecto.
     * @return número más alto o {@code null} si aún no hay entregas.
     */
    @Query("""
            select max(s.submissionNumber) from ProjectSubmission s
            where s.user.id = :userId and s.project.id = :projectId
            """)
    Integer findMaxSubmissionNumber(@Param("userId") Long userId,
                                    @Param("projectId") Long projectId);

    /**
     * Indica si el estudiante ya tiene alguna entrega aprobada en el
     * proyecto. Se utiliza para otorgar la XP una única vez.
     *
     * @param userId    identificador del estudiante.
     * @param projectId identificador del proyecto.
     * @return {@code true} cuando ya existe una entrega aprobada previa.
     */
    @Query("""
            select case when count(s) > 0 then true else false end
            from ProjectSubmission s
            where s.user.id = :userId
              and s.project.id = :projectId
              and s.status = tech.impulso.projects.entity.ProjectSubmissionStatus.APROBADA
            """)
    boolean hasApprovedSubmission(@Param("userId") Long userId,
                                  @Param("projectId") Long projectId);

    /**
     * Cuenta cuántos proyectos distintos ha aprobado el estudiante en
     * toda la plataforma. Se utiliza en el panel de progreso.
     *
     * @param userId identificador del estudiante.
     * @return cantidad de proyectos aprobados (distintos).
     */
    @Query("""
            select count(distinct s.project.id) from ProjectSubmission s
            where s.user.id = :userId
              and s.status = tech.impulso.projects.entity.ProjectSubmissionStatus.APROBADA
            """)
    long countDistinctApprovedProjects(@Param("userId") Long userId);

    /**
     * Cuenta las entregas pendientes de revisión (estados
     * {@code ENVIADA} y {@code EN_REVISION}) sobre proyectos del
     * instructor indicado. Se utiliza en el panel del instructor
     * (RF-032).
     */
    @Query("""
            select count(s) from ProjectSubmission s
            where s.project.instructor.id = :instructorId
              and s.status in (
                    tech.impulso.projects.entity.ProjectSubmissionStatus.ENVIADA,
                    tech.impulso.projects.entity.ProjectSubmissionStatus.EN_REVISION
              )
            """)
    long countPendingByInstructor(@Param("instructorId") Long instructorId);
}
