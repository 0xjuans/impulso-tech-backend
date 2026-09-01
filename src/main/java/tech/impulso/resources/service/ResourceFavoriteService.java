package tech.impulso.resources.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.content.ContentStatus;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.resources.dto.FavoriteResourceResponse;
import tech.impulso.resources.entity.EducationalResource;
import tech.impulso.resources.entity.ResourceFavorite;
import tech.impulso.resources.repository.EducationalResourceRepository;
import tech.impulso.resources.repository.ResourceFavoriteRepository;
import tech.impulso.users.entity.User;

/**
 * Servicio con las operaciones sobre recursos favoritos (RF-025).
 *
 * <p>Un estudiante sólo puede marcar como favorito un recurso que se
 * encuentre publicado. Al desmarcar el favorito el recurso permanece en
 * la biblioteca; sólo se elimina la relación de favorito.</p>
 */
@Service
public class ResourceFavoriteService {

    private final ResourceFavoriteRepository favoriteRepository;
    private final EducationalResourceRepository resourceRepository;
    private final CurrentUserService currentUserService;

    public ResourceFavoriteService(ResourceFavoriteRepository favoriteRepository,
                                   EducationalResourceRepository resourceRepository,
                                   CurrentUserService currentUserService) {
        this.favoriteRepository = favoriteRepository;
        this.resourceRepository = resourceRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Marca un recurso como favorito para el usuario autenticado. La
     * operación es idempotente: si el recurso ya estaba marcado devuelve
     * el favorito existente sin duplicarlo.
     *
     * @param resourceId identificador del recurso a marcar.
     * @return favorito resultante.
     */
    @Transactional
    public FavoriteResourceResponse add(Long resourceId) {
        User user = currentUserService.requireAuthenticatedUser();
        EducationalResource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "El recurso indicado no existe."));

        if (resource.getStatus() != ContentStatus.PUBLICADO) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "El recurso no está publicado y no puede marcarse como favorito.");
        }

        ResourceFavorite favorite = favoriteRepository.findByUserIdAndResourceId(user.getId(), resourceId)
                .orElseGet(() -> {
                    ResourceFavorite created = new ResourceFavorite();
                    created.setUser(user);
                    created.setResource(resource);
                    return favoriteRepository.save(created);
                });
        return FavoriteResourceResponse.from(favorite);
    }

    /**
     * Elimina un recurso de la lista de favoritos del usuario
     * autenticado. Si el recurso no estaba marcado se lanza 404.
     *
     * @param resourceId identificador del recurso a quitar.
     */
    @Transactional
    public void remove(Long resourceId) {
        User user = currentUserService.requireAuthenticatedUser();
        ResourceFavorite favorite = favoriteRepository.findByUserIdAndResourceId(user.getId(), resourceId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El recurso no se encuentra en tu lista de favoritos."));
        favoriteRepository.delete(favorite);
    }

    /**
     * Devuelve la lista paginada de favoritos del usuario autenticado.
     *
     * @param pageable configuración de paginación y ordenamiento.
     * @return página con los favoritos del usuario.
     */
    @Transactional(readOnly = true)
    public PagedResponse<FavoriteResourceResponse> listMine(Pageable pageable) {
        User user = currentUserService.requireAuthenticatedUser();
        Page<ResourceFavorite> page = favoriteRepository.findByUserId(user.getId(), pageable);
        return PagedResponse.from(page, FavoriteResourceResponse::from);
    }
}
