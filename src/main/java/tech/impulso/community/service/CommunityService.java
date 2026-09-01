package tech.impulso.community.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.admin.dto.PagedResponse;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.community.dto.CommunityPostDetailResponse;
import tech.impulso.community.dto.CommunityPostResponse;
import tech.impulso.community.dto.CommunityReplyResponse;
import tech.impulso.community.dto.CreatePostRequest;
import tech.impulso.community.dto.CreateReplyRequest;
import tech.impulso.community.dto.UpdatePostRequest;
import tech.impulso.community.dto.UpdateReplyRequest;
import tech.impulso.community.entity.CommunityHelpfulVote;
import tech.impulso.community.entity.CommunityPost;
import tech.impulso.community.entity.CommunityReply;
import tech.impulso.community.entity.RelatedContentType;
import tech.impulso.community.repository.CommunityHelpfulVoteRepository;
import tech.impulso.community.repository.CommunityPostRepository;
import tech.impulso.community.repository.CommunityReplyRepository;
import tech.impulso.users.entity.Role;
import tech.impulso.users.entity.User;

import java.util.List;

/**
 * Servicio con las operaciones sobre publicaciones y respuestas de la
 * comunidad (RF-034).
 *
 * <p>Aplica las reglas de propiedad: cada usuario puede editar y
 * eliminar únicamente sus propias publicaciones y respuestas; el
 * administrador puede intervenir en cualquiera. Marcar una respuesta
 * como aceptada queda reservado al autor de la publicación o a un
 * administrador.</p>
 */
@Service
public class CommunityService {

    private final CommunityPostRepository postRepository;
    private final CommunityReplyRepository replyRepository;
    private final CommunityHelpfulVoteRepository helpfulVoteRepository;
    private final CurrentUserService currentUserService;

    public CommunityService(CommunityPostRepository postRepository,
                            CommunityReplyRepository replyRepository,
                            CommunityHelpfulVoteRepository helpfulVoteRepository,
                            CurrentUserService currentUserService) {
        this.postRepository = postRepository;
        this.replyRepository = replyRepository;
        this.helpfulVoteRepository = helpfulVoteRepository;
        this.currentUserService = currentUserService;
    }

    /**
     * Lista las publicaciones de la comunidad aplicando filtros
     * opcionales.
     */
    @Transactional(readOnly = true)
    public PagedResponse<CommunityPostResponse> listPosts(String search,
                                                          RelatedContentType relatedType,
                                                          Long relatedId,
                                                          Long authorId,
                                                          Pageable pageable) {
        Page<CommunityPost> page = postRepository.search(normalize(search), relatedType, relatedId, authorId, pageable);
        return PagedResponse.from(page, CommunityPostResponse::from);
    }

    /**
     * Devuelve el detalle de una publicación con todas sus respuestas.
     */
    @Transactional(readOnly = true)
    public CommunityPostDetailResponse getPost(Long postId) {
        CommunityPost post = loadPost(postId);
        List<CommunityReply> replies = replyRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return CommunityPostDetailResponse.from(post, replies);
    }

    /**
     * Crea una nueva publicación asignando como autor al usuario
     * autenticado.
     */
    @Transactional
    public CommunityPostResponse createPost(CreatePostRequest request) {
        User author = currentUserService.requireAuthenticatedUser();

        CommunityPost post = new CommunityPost();
        post.setAuthor(author);
        post.setTitle(request.title().trim());
        post.setDescription(request.description());
        post.setCodeSnippet(request.codeSnippet());
        post.setTags(nullIfBlank(request.tags()));
        post.setRelatedType(request.relatedType());
        post.setRelatedId(request.relatedId());

        return CommunityPostResponse.from(postRepository.save(post));
    }

    /**
     * Actualiza los campos permitidos de una publicación existente.
     */
    @Transactional
    public CommunityPostResponse updatePost(Long postId, UpdatePostRequest request) {
        CommunityPost post = loadPost(postId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEditPost(post, current);

        if (request.title() != null) {
            post.setTitle(request.title().trim());
        }
        if (request.description() != null) {
            post.setDescription(request.description());
        }
        if (request.codeSnippet() != null) {
            post.setCodeSnippet(request.codeSnippet());
        }
        if (request.tags() != null) {
            post.setTags(nullIfBlank(request.tags()));
        }

        return CommunityPostResponse.from(post);
    }

    /**
     * Elimina una publicación. Las respuestas asociadas se eliminan en
     * cascada por la base de datos.
     */
    @Transactional
    public void deletePost(Long postId) {
        CommunityPost post = loadPost(postId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanDeletePost(post, current);
        postRepository.delete(post);
    }

    /**
     * Publica una respuesta en la publicación indicada.
     */
    @Transactional
    public CommunityReplyResponse createReply(Long postId, CreateReplyRequest request) {
        CommunityPost post = loadPost(postId);
        User author = currentUserService.requireAuthenticatedUser();

        CommunityReply reply = new CommunityReply();
        reply.setPost(post);
        reply.setAuthor(author);
        reply.setContent(request.content());
        reply.setCodeSnippet(request.codeSnippet());
        reply = replyRepository.save(reply);

        return CommunityReplyResponse.from(reply, post.getAcceptedReplyId());
    }

    /**
     * Actualiza los campos permitidos de una respuesta existente.
     */
    @Transactional
    public CommunityReplyResponse updateReply(Long replyId, UpdateReplyRequest request) {
        CommunityReply reply = loadReply(replyId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanEditReply(reply, current);

        if (request.content() != null) {
            reply.setContent(request.content());
        }
        if (request.codeSnippet() != null) {
            reply.setCodeSnippet(request.codeSnippet());
        }

        return CommunityReplyResponse.from(reply, reply.getPost().getAcceptedReplyId());
    }

    /**
     * Elimina una respuesta.
     */
    @Transactional
    public void deleteReply(Long replyId) {
        CommunityReply reply = loadReply(replyId);
        User current = currentUserService.requireAuthenticatedUser();
        ensureCanDeleteReply(reply, current);
        CommunityPost post = reply.getPost();
        if (reply.getId().equals(post.getAcceptedReplyId())) {
            post.setAcceptedReplyId(null);
        }
        replyRepository.delete(reply);
    }

    /**
     * Alterna la marca de "útil" del usuario autenticado sobre la
     * respuesta indicada: si aún no la había marcado la registra y
     * suma; si ya la había marcado la retira y resta.
     *
     * @param replyId identificador de la respuesta.
     * @return respuesta actualizada.
     */
    @Transactional
    public CommunityReplyResponse toggleHelpful(Long replyId) {
        CommunityReply reply = loadReply(replyId);
        User user = currentUserService.requireAuthenticatedUser();

        var existing = helpfulVoteRepository.findByReplyIdAndUserId(replyId, user.getId());
        if (existing.isPresent()) {
            helpfulVoteRepository.delete(existing.get());
            reply.setHelpfulCount(Math.max(0, reply.getHelpfulCount() - 1));
        } else {
            CommunityHelpfulVote vote = new CommunityHelpfulVote();
            vote.setReply(reply);
            vote.setUser(user);
            helpfulVoteRepository.save(vote);
            reply.setHelpfulCount(reply.getHelpfulCount() + 1);
        }

        return CommunityReplyResponse.from(reply, reply.getPost().getAcceptedReplyId());
    }

    /**
     * Marca una respuesta como aceptada por el autor de la publicación
     * o por un administrador. Enviar {@code replyId=null} limpia la
     * aceptación previa.
     *
     * @param postId  identificador de la publicación.
     * @param replyId identificador de la respuesta a aceptar; puede ser
     *                {@code null} para desmarcar.
     * @return publicación actualizada.
     */
    @Transactional
    public CommunityPostResponse setAcceptedReply(Long postId, Long replyId) {
        CommunityPost post = loadPost(postId);
        User current = currentUserService.requireAuthenticatedUser();
        boolean isAuthor = post.getAuthor().getId().equals(current.getId());
        boolean isAdmin = current.getRole() == Role.ADMINISTRADOR;
        if (!isAuthor && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el autor de la publicación o un administrador puede marcar la respuesta aceptada.");
        }

        if (replyId != null) {
            CommunityReply reply = loadReply(replyId);
            if (!reply.getPost().getId().equals(postId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "La respuesta indicada no pertenece a esta publicación.");
            }
        }
        post.setAcceptedReplyId(replyId);
        return CommunityPostResponse.from(post);
    }

    private CommunityPost loadPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La publicación indicada no existe."));
    }

    private CommunityReply loadReply(Long replyId) {
        return replyRepository.findById(replyId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "La respuesta indicada no existe."));
    }

    private void ensureCanEditPost(CommunityPost post, User user) {
        if (!post.getAuthor().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el autor puede modificar esta publicación.");
        }
    }

    private void ensureCanDeletePost(CommunityPost post, User user) {
        boolean isOwner = post.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el autor o un administrador puede eliminar esta publicación.");
        }
    }

    private void ensureCanEditReply(CommunityReply reply, User user) {
        if (!reply.getAuthor().getId().equals(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el autor puede modificar esta respuesta.");
        }
    }

    private void ensureCanDeleteReply(CommunityReply reply, User user) {
        boolean isOwner = reply.getAuthor().getId().equals(user.getId());
        boolean isAdmin = user.getRole() == Role.ADMINISTRADOR;
        if (!isOwner && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "Solo el autor o un administrador puede eliminar esta respuesta.");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String nullIfBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
