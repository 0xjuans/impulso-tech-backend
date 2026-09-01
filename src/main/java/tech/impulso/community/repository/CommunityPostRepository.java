package tech.impulso.community.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.community.entity.CommunityPost;
import tech.impulso.community.entity.RelatedContentType;

/**
 * Repositorio de persistencia para {@link CommunityPost}.
 */
@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    /**
     * Búsqueda paginada de publicaciones aplicando filtros opcionales.
     *
     * @param search      fragmento a buscar en título, descripción o etiquetas.
     * @param relatedType tipo del contenido relacionado, opcional.
     * @param relatedId   identificador del contenido relacionado, opcional.
     * @param authorId    identificador del autor, opcional.
     * @param pageable    configuración de paginación y ordenamiento.
     * @return página con las publicaciones coincidentes.
     */
    @Query("""
            select p from CommunityPost p
            where (:search      is null or
                   lower(p.title)       like lower(concat('%', :search, '%')) or
                   lower(p.description) like lower(concat('%', :search, '%')) or
                   lower(p.tags)        like lower(concat('%', :search, '%')))
              and (:relatedType is null or p.relatedType = :relatedType)
              and (:relatedId   is null or p.relatedId   = :relatedId)
              and (:authorId    is null or p.author.id   = :authorId)
            """)
    Page<CommunityPost> search(@Param("search") String search,
                               @Param("relatedType") RelatedContentType relatedType,
                               @Param("relatedId") Long relatedId,
                               @Param("authorId") Long authorId,
                               Pageable pageable);
}
