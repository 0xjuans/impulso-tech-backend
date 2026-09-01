package tech.impulso.enrollments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.enrollments.entity.Enrollment;
import tech.impulso.enrollments.entity.EnrollmentStatus;

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
}
