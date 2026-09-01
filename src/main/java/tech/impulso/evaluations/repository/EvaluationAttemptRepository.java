package tech.impulso.evaluations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.evaluations.entity.EvaluationAttempt;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de persistencia para {@link EvaluationAttempt}.
 */
@Repository
public interface EvaluationAttemptRepository extends JpaRepository<EvaluationAttempt, Long> {

    /**
     * Devuelve el intento en curso (sin fecha de finalización) del
     * usuario sobre una evaluación específica.
     *
     * @param userId       identificador del usuario.
     * @param evaluationId identificador de la evaluación.
     * @return intento en curso, si existe.
     */
    @Query("""
            select a from EvaluationAttempt a
            where a.user.id = :userId
              and a.evaluation.id = :evaluationId
              and a.finishedAt is null
            """)
    Optional<EvaluationAttempt> findInProgress(@Param("userId") Long userId,
                                               @Param("evaluationId") Long evaluationId);

    /**
     * Cuenta los intentos finalizados de un usuario en una evaluación.
     * Los intentos en curso no cuentan hasta que se envíen o expiren.
     *
     * @param userId       identificador del usuario.
     * @param evaluationId identificador de la evaluación.
     * @return cantidad de intentos finalizados.
     */
    @Query("""
            select count(a) from EvaluationAttempt a
            where a.user.id = :userId
              and a.evaluation.id = :evaluationId
              and a.finishedAt is not null
            """)
    long countFinished(@Param("userId") Long userId,
                       @Param("evaluationId") Long evaluationId);

    /**
     * Devuelve el historial de intentos finalizados del usuario sobre
     * una evaluación, ordenados de más reciente a más antiguo.
     *
     * @param userId       identificador del usuario.
     * @param evaluationId identificador de la evaluación.
     * @return intentos finalizados del usuario.
     */
    @Query("""
            select a from EvaluationAttempt a
            where a.user.id = :userId
              and a.evaluation.id = :evaluationId
              and a.finishedAt is not null
            order by a.finishedAt desc
            """)
    List<EvaluationAttempt> findFinishedByUser(@Param("userId") Long userId,
                                               @Param("evaluationId") Long evaluationId);

    /**
     * Indica si el usuario ya aprobó la evaluación en algún intento
     * previo.
     *
     * @param userId       identificador del usuario.
     * @param evaluationId identificador de la evaluación.
     * @return {@code true} cuando existe al menos un intento aprobado.
     */
    @Query("""
            select case when count(a) > 0 then true else false end
            from EvaluationAttempt a
            where a.user.id = :userId
              and a.evaluation.id = :evaluationId
              and a.passed = true
            """)
    boolean hasPassedAttempt(@Param("userId") Long userId,
                             @Param("evaluationId") Long evaluationId);
}
