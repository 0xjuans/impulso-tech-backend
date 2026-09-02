package tech.impulso.challenges.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.challenges.entity.ChallengeAttempt;
import tech.impulso.challenges.entity.ChallengeAttemptStatus;

import java.util.List;

/**
 * Repositorio de persistencia para {@link ChallengeAttempt}.
 */
@Repository
public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttempt, Long> {

    /**
     * Devuelve los intentos del estudiante sobre un reto ordenados de
     * más reciente a más antiguo.
     */
    List<ChallengeAttempt> findByUserIdAndChallengeIdOrderByAttemptNumberDesc(Long userId, Long challengeId);

    /**
     * Devuelve los intentos recibidos en un reto, con soporte de
     * filtro por estado. Se utiliza en el panel del instructor.
     */
    @Query("""
            select a from ChallengeAttempt a
            where a.challenge.id = :challengeId
              and (:status is null or a.status = :status)
            """)
    Page<ChallengeAttempt> findByChallenge(@Param("challengeId") Long challengeId,
                                           @Param("status") ChallengeAttemptStatus status,
                                           Pageable pageable);

    /**
     * Devuelve el número más alto de intento registrado por el
     * estudiante en un reto. Se utiliza al crear un intento nuevo para
     * asignarle el siguiente número.
     */
    @Query("""
            select max(a.attemptNumber) from ChallengeAttempt a
            where a.user.id = :userId and a.challenge.id = :challengeId
            """)
    Integer findMaxAttemptNumber(@Param("userId") Long userId,
                                 @Param("challengeId") Long challengeId);

    /**
     * Indica si el estudiante ya tiene algún intento aprobado en el
     * reto. Se utiliza para otorgar la XP una única vez.
     */
    @Query("""
            select case when count(a) > 0 then true else false end
            from ChallengeAttempt a
            where a.user.id = :userId
              and a.challenge.id = :challengeId
              and a.status = tech.impulso.challenges.entity.ChallengeAttemptStatus.APROBADO
            """)
    boolean hasApprovedAttempt(@Param("userId") Long userId,
                               @Param("challengeId") Long challengeId);

    /**
     * Cuenta cuántos retos distintos ha resuelto correctamente el
     * estudiante en toda la plataforma. Se utiliza en el panel de
     * progreso.
     *
     * @param userId identificador del estudiante.
     * @return cantidad de retos aprobados (distintos).
     */
    @Query("""
            select count(distinct a.challenge.id) from ChallengeAttempt a
            where a.user.id = :userId
              and a.status = tech.impulso.challenges.entity.ChallengeAttemptStatus.APROBADO
            """)
    long countDistinctSolvedChallenges(@Param("userId") Long userId);
}
