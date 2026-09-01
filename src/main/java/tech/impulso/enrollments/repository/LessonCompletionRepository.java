package tech.impulso.enrollments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.enrollments.entity.LessonCompletion;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de persistencia para {@link LessonCompletion}.
 */
@Repository
public interface LessonCompletionRepository extends JpaRepository<LessonCompletion, Long> {

    /**
     * Indica si el estudiante ya marcó como completada una lección.
     *
     * @param userId   identificador del estudiante.
     * @param lessonId identificador de la lección.
     * @return {@code true} cuando ya existe la marca de finalización.
     */
    boolean existsByUserIdAndLessonId(Long userId, Long lessonId);

    /**
     * Cuenta el total de lecciones que un usuario ha completado en toda
     * la plataforma. Se utiliza como métrica agregada de progreso, por
     * ejemplo para evaluar insignias por hitos de aprendizaje.
     *
     * @param userId identificador del usuario.
     * @return cantidad total de lecciones completadas.
     */
    long countByUserId(Long userId);

    /**
     * Localiza la marca de finalización de una lección por parte de un
     * estudiante.
     *
     * @param userId   identificador del estudiante.
     * @param lessonId identificador de la lección.
     * @return la marca de finalización si existe.
     */
    Optional<LessonCompletion> findByUserIdAndLessonId(Long userId, Long lessonId);

    /**
     * Cuenta cuántas lecciones publicadas y obligatorias de un curso ha
     * completado el estudiante. Sólo se consideran las lecciones y los
     * módulos que se encuentran en estado {@code PUBLICADO}.
     *
     * @param userId   identificador del estudiante.
     * @param courseId identificador del curso.
     * @return cantidad de lecciones obligatorias completadas.
     */
    @Query("""
            select count(lc) from LessonCompletion lc
            where lc.user.id = :userId
              and lc.lesson.module.course.id = :courseId
              and lc.lesson.optional = false
              and lc.lesson.status = tech.impulso.common.content.ContentStatus.PUBLICADO
              and lc.lesson.module.status = tech.impulso.common.content.ContentStatus.PUBLICADO
            """)
    long countMandatoryCompletedInCourse(@Param("userId") Long userId,
                                         @Param("courseId") Long courseId);

    /**
     * Devuelve los identificadores de las lecciones de un curso que un
     * estudiante ha completado. Se utiliza para presentar el detalle del
     * progreso al estudiante.
     *
     * @param userId   identificador del estudiante.
     * @param courseId identificador del curso.
     * @return identificadores de las lecciones completadas.
     */
    @Query("""
            select lc.lesson.id from LessonCompletion lc
            where lc.user.id = :userId
              and lc.lesson.module.course.id = :courseId
            """)
    List<Long> findCompletedLessonIdsInCourse(@Param("userId") Long userId,
                                              @Param("courseId") Long courseId);
}
