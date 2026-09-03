package tech.impulso.labs.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.labs.entity.LabSubmission;

import java.util.List;

/**
 * Repositorio de persistencia para {@link LabSubmission}.
 */
@Repository
public interface LabSubmissionRepository extends JpaRepository<LabSubmission, Long> {

    /**
     * Devuelve las entregas de un estudiante sobre un laboratorio,
     * ordenadas de la más reciente a la más antigua.
     */
    List<LabSubmission> findByLabIdAndUserIdOrderBySubmissionNumberDesc(Long labId, Long userId);

    /**
     * Devuelve el número más alto de entrega ya registrado para el par
     * (laboratorio, usuario). Se utiliza para asignar el siguiente
     * número al crear una entrega nueva.
     */
    @Query("""
            select max(s.submissionNumber) from LabSubmission s
            where s.lab.id = :labId and s.user.id = :userId
            """)
    Integer findMaxSubmissionNumber(@Param("labId") Long labId,
                                    @Param("userId") Long userId);
}
