package tech.impulso.resources.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.resources.entity.ResourceFavorite;

import java.util.Optional;

/**
 * Repositorio de persistencia para {@link ResourceFavorite}.
 */
@Repository
public interface ResourceFavoriteRepository extends JpaRepository<ResourceFavorite, Long> {

    /**
     * Devuelve los favoritos del usuario indicado, ordenados según la
     * configuración de paginación.
     *
     * @param userId   identificador del usuario.
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con los favoritos del usuario.
     */
    Page<ResourceFavorite> findByUserId(Long userId, Pageable pageable);

    /**
     * Localiza el favorito existente para un par usuario/recurso, si
     * existe.
     *
     * @param userId     identificador del usuario.
     * @param resourceId identificador del recurso.
     * @return el favorito si el usuario ya lo marcó.
     */
    Optional<ResourceFavorite> findByUserIdAndResourceId(Long userId, Long resourceId);

    /**
     * Indica si el usuario ya marcó el recurso como favorito.
     *
     * @param userId     identificador del usuario.
     * @param resourceId identificador del recurso.
     * @return {@code true} cuando ya existe la marca.
     */
    boolean existsByUserIdAndResourceId(Long userId, Long resourceId);
}
