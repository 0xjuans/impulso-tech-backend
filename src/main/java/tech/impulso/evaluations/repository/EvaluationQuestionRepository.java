package tech.impulso.evaluations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.evaluations.entity.EvaluationQuestion;

import java.util.List;

/**
 * Repositorio de persistencia para {@link EvaluationQuestion}.
 */
@Repository
public interface EvaluationQuestionRepository extends JpaRepository<EvaluationQuestion, Long> {

    /**
     * Devuelve las preguntas de una evaluación ordenadas por su
     * posición.
     *
     * @param evaluationId identificador de la evaluación.
     * @return preguntas coincidentes.
     */
    List<EvaluationQuestion> findByEvaluationIdOrderByOrderIndexAsc(Long evaluationId);
}
