package tech.impulso.resources.dto;

import tech.impulso.resources.entity.ResourceFavorite;

import java.time.OffsetDateTime;

/**
 * Representación pública de un favorito, con el detalle del recurso
 * asociado y la fecha en que fue marcado.
 *
 * @param favoritedAt momento en que se agregó a favoritos.
 * @param resource    representación pública del recurso.
 */
public record FavoriteResourceResponse(
        OffsetDateTime favoritedAt,
        EducationalResourceResponse resource
) {

    /**
     * Construye la representación pública a partir de la entidad
     * persistente.
     *
     * @param favorite entidad a convertir.
     * @return DTO listo para devolver desde la API.
     */
    public static FavoriteResourceResponse from(ResourceFavorite favorite) {
        return new FavoriteResourceResponse(
                favorite.getCreatedAt(),
                EducationalResourceResponse.from(favorite.getResource())
        );
    }
}
