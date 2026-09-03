package tech.impulso.enrollments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.enrollments.entity.Enrollment;
import tech.impulso.enrollments.entity.EnrollmentStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de persistencia para {@link Enrollment}.
 */
@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    /**
     * Localiza la inscripción de un estudiante en un curso específico.
     *
     * @param userId   identificador del estudiante.
     * @param courseId identificador del curso.
     * @return la inscripción si existe.
     */
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);

    /**
     * Devuelve las inscripciones de un estudiante ordenadas por la
     * configuración de paginación.
     *
     * @param userId   identificador del estudiante.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con las inscripciones del estudiante.
     */
    Page<Enrollment> findByUserId(Long userId, Pageable pageable);

    /**
     * Cuenta cuántas inscripciones del usuario se encuentran en el
     * estado indicado. Se utiliza como métrica de progreso agregado
     * (por ejemplo, cursos completados para el sistema de insignias).
     *
     * @param userId identificador del usuario.
     * @param status estado a contar.
     * @return cantidad de inscripciones en ese estado.
     */
    long countByUserIdAndStatus(Long userId, EnrollmentStatus status);

    /**
     * Cuenta el total de inscripciones activas del usuario, sin filtrar
     * por estado. Se utiliza como métrica en el panel de progreso.
     *
     * @param userId identificador del usuario.
     * @return cantidad total de inscripciones.
     */
    long countByUserId(Long userId);

    /**
     * Cuenta el total de inscripciones registradas en cursos del
     * instructor indicado. Se utiliza en el panel del instructor
     * (RF-032).
     */
    @Query("""
            select count(e) from Enrollment e
            where e.course.instructor.id = :instructorId
            """)
    long countByInstructor(@Param("instructorId") Long instructorId);

    /**
     * Cuenta el número de estudiantes distintos inscritos en algún curso
     * del instructor indicado.
     */
    @Query("""
            select count(distinct e.user.id) from Enrollment e
            where e.course.instructor.id = :instructorId
            """)
    long countDistinctStudentsByInstructor(@Param("instructorId") Long instructorId);

    /**
     * Cuenta cuántas inscripciones existen en el estado indicado. Se
     * utiliza para la tasa de finalización global (RF-058).
     */
    long countByStatus(EnrollmentStatus status);

    /**
     * Serie temporal de inscripciones diarias en los últimos
     * {@code days} días. Devuelve tuplas
     * {@code [fecha (java.sql.Date), cantidad (Number)]}.
     */
    @Query(value = """
            select date_trunc('day', started_at)::date as day, count(*) as total
            from enrollments
            where started_at >= now() - make_interval(days => :days)
            group by day
            order by day
            """, nativeQuery = true)
    List<Object[]> countEnrollmentsByDay(@Param("days") int days);

    /**
     * Serie temporal de inscripciones diarias en cursos del instructor
     * indicado.
     */
    @Query(value = """
            select date_trunc('day', e.started_at)::date as day, count(*) as total
            from enrollments e
            join courses c on c.id = e.course_id
            where c.instructor_id = :instructorId
              and e.started_at >= now() - make_interval(days => :days)
            group by day
            order by day
            """, nativeQuery = true)
    List<Object[]> countEnrollmentsByDayForInstructor(@Param("instructorId") Long instructorId,
                                                      @Param("days") int days);

    /**
     * Devuelve los cursos con más inscripciones de toda la plataforma,
     * con su nombre y la cantidad total. Tuplas
     * {@code [id (Number), name (String), total (Number)]}.
     */
    @Query(value = """
            select c.id, c.name, count(e.id) as total
            from courses c
            left join enrollments e on e.course_id = c.id
            group by c.id, c.name
            order by total desc, c.id asc
            limit :max
            """, nativeQuery = true)
    List<Object[]> findTopCoursesByEnrollments(@Param("max") int max);

    /**
     * Devuelve los cursos del instructor con su cantidad de inscripciones
     * y cuántas se encuentran en estado {@code COMPLETADO}. Tuplas
     * {@code [id, name, totalEnrollments, completedEnrollments]}.
     */
    @Query(value = """
            select c.id, c.name,
                   count(e.id) as total,
                   coalesce(sum(case when e.status = 'COMPLETADO' then 1 else 0 end), 0) as completed
            from courses c
            left join enrollments e on e.course_id = c.id
            where c.instructor_id = :instructorId
            group by c.id, c.name
            order by total desc, c.id asc
            limit :max
            """, nativeQuery = true)
    List<Object[]> findInstructorCourseEnrollmentStats(@Param("instructorId") Long instructorId,
                                                       @Param("max") int max);

    /**
     * Devuelve los cursos completados por el estudiante, cargando el
     * curso asociado. Se utiliza para construir el perfil de
     * recomendaciones (RF-053).
     */
    @Query("""
            select e.course from Enrollment e
            where e.user.id = :userId
              and e.status = tech.impulso.enrollments.entity.EnrollmentStatus.COMPLETADO
            """)
    List<tech.impulso.courses.entity.Course> findCompletedCoursesByUser(@Param("userId") Long userId);

    /**
     * Devuelve los cursos actualmente en curso o iniciados por el
     * estudiante. Se utiliza para inferir sus intereses actuales.
     */
    @Query("""
            select e.course from Enrollment e
            where e.user.id = :userId
              and e.status <> tech.impulso.enrollments.entity.EnrollmentStatus.COMPLETADO
            """)
    List<tech.impulso.courses.entity.Course> findInProgressCoursesByUser(@Param("userId") Long userId);
}
