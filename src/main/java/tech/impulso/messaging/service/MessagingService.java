package tech.impulso.messaging.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.impulso.common.exception.BusinessException;
import tech.impulso.common.security.CurrentUserService;
import tech.impulso.messaging.dto.ConversationResponse;
import tech.impulso.messaging.dto.MessageResponse;
import tech.impulso.messaging.dto.SendMessageRequest;
import tech.impulso.messaging.entity.Message;
import tech.impulso.messaging.entity.MessageConversation;
import tech.impulso.messaging.repository.MessageConversationRepository;
import tech.impulso.messaging.repository.MessageRepository;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;
import tech.impulso.users.repository.UserRepository;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Servicio de mensajería directa entre usuarios (RF-061).
 *
 * <p>Orquesta la apertura de conversaciones (con normalización del par
 * de participantes), el envío y consulta de mensajes, la marca de
 * leído y la aplicación del límite anti-abuso. Ambos participantes
 * tienen acceso simétrico a la conversación; el administrador puede
 * leer cualquier conversación con fines de auditoría (RF-031).</p>
 */
@Service
public class MessagingService {

    /** Longitud máxima de la vista previa del último mensaje. */
    private static final int PREVIEW_LENGTH = 140;

    /** Cantidad máxima de mensajes que puede enviar un usuario por hora. */
    private static final int MAX_MESSAGES_PER_HOUR = 100;

    private final MessageConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final tech.impulso.preferences.repository.UserPreferencesRepository preferencesRepository;
    private final tech.impulso.auth.service.EmailService emailService;
    private final tech.impulso.notifications.service.NotificationSseBroadcaster sseBroadcaster;

    public MessagingService(MessageConversationRepository conversationRepository,
                            MessageRepository messageRepository,
                            UserRepository userRepository,
                            CurrentUserService currentUserService,
                            tech.impulso.preferences.repository.UserPreferencesRepository preferencesRepository,
                            tech.impulso.auth.service.EmailService emailService,
                            tech.impulso.notifications.service.NotificationSseBroadcaster sseBroadcaster) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.preferencesRepository = preferencesRepository;
        this.emailService = emailService;
        this.sseBroadcaster = sseBroadcaster;
    }

    /**
     * Devuelve las conversaciones del usuario autenticado, con vista
     * previa y contador de mensajes no leídos.
     */
    @Transactional(readOnly = true)
    public Page<ConversationResponse> listMyConversations(Pageable pageable) {
        User user = currentUserService.requireAuthenticatedUser();
        return conversationRepository.findByParticipantId(user.getId(), pageable)
                .map(conversation -> {
                    String preview = messageRepository
                            .findFirstByConversationIdOrderBySentAtDesc(conversation.getId())
                            .map(m -> truncate(m.getContent()))
                            .orElse(null);
                    long unread = messageRepository.countUnreadFor(conversation.getId(), user.getId());
                    return ConversationResponse.of(conversation, user.getId(), preview, unread);
                });
    }

    /**
     * Abre (o recupera) la conversación entre el usuario autenticado y
     * el destinatario indicado. La operación es idempotente.
     */
    @Transactional
    public ConversationResponse openConversation(Long recipientId) {
        User user = currentUserService.requireAuthenticatedUser();
        if (recipientId == null || recipientId.equals(user.getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Debes seleccionar a otro usuario distinto a ti.");
        }
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "El usuario indicado no existe."));
        if (recipient.getStatus() != UserStatus.ACTIVA) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "El usuario indicado no está disponible para recibir mensajes.");
        }

        MessageConversation conversation = findOrCreate(user, recipient);
        long unread = messageRepository.countUnreadFor(conversation.getId(), user.getId());
        String preview = messageRepository
                .findFirstByConversationIdOrderBySentAtDesc(conversation.getId())
                .map(m -> truncate(m.getContent()))
                .orElse(null);
        return ConversationResponse.of(conversation, user.getId(), preview, unread);
    }

    /**
     * Devuelve los mensajes de una conversación, del más reciente al
     * más antiguo. Requiere que el usuario autenticado sea participante
     * o administrador.
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> listMessages(Long conversationId, Pageable pageable) {
        User user = currentUserService.requireAuthenticatedUser();
        requireVisibleConversation(conversationId, user);
        return messageRepository.findByConversationIdOrderBySentAtDesc(conversationId, pageable)
                .map(MessageResponse::from);
    }

    /**
     * Envía un mensaje en la conversación indicada. El emisor queda
     * registrado como el usuario autenticado.
     */
    @Transactional
    public MessageResponse sendMessage(Long conversationId, SendMessageRequest request) {
        User user = currentUserService.requireAuthenticatedUser();
        MessageConversation conversation = requireParticipant(conversationId, user);
        enforceRateLimit(user.getId());

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(user);
        message.setContent(request.content().strip());
        message = messageRepository.save(message);

        conversation.setLastMessageAt(message.getSentAt());
        conversationRepository.save(conversation);

        notifyRecipientByEmail(conversation, user, message);
        broadcastRealtime(conversation, user, message);

        return MessageResponse.from(message);
    }

    /**
     * Empuja el mensaje recién guardado al destinatario por SSE tras el
     * commit, para que su inbox se actualice sin recargar. Si el
     * emisor SSE falla la operación es silenciosa: el mensaje ya quedó
     * persistido y el destinatario lo verá al abrir la conversación.
     */
    private void broadcastRealtime(MessageConversation conversation,
                                   User sender,
                                   Message message) {
        User recipient = conversation.getParticipantLow().getId().equals(sender.getId())
                ? conversation.getParticipantHigh()
                : conversation.getParticipantLow();
        if (recipient == null || recipient.getId().equals(sender.getId())) {
            return;
        }
        Long recipientId = recipient.getId();
        MessageResponse payload = MessageResponse.from(message);

        if (org.springframework.transaction.support.TransactionSynchronizationManager
                .isSynchronizationActive()) {
            org.springframework.transaction.support.TransactionSynchronizationManager
                    .registerSynchronization(
                            new org.springframework.transaction.support.TransactionSynchronization() {
                                @Override
                                public void afterCommit() {
                                    sseBroadcaster.broadcastEvent(
                                            recipientId,
                                            tech.impulso.notifications.service.NotificationSseBroadcaster.EVENT_MESSAGE,
                                            payload);
                                }
                            });
        } else {
            sseBroadcaster.broadcastEvent(
                    recipientId,
                    tech.impulso.notifications.service.NotificationSseBroadcaster.EVENT_MESSAGE,
                    payload);
        }
    }

    /**
     * Envía un correo al destinatario del mensaje si mantiene activa la
     * preferencia {@code notifyByEmail}. El envío se difiere a
     * {@code afterCommit} para no bloquear la transacción y para que un
     * fallo del proveedor SMTP nunca cancele el guardado del mensaje.
     */
    private void notifyRecipientByEmail(MessageConversation conversation,
                                        User sender,
                                        Message message) {
        User recipient = conversation.getParticipantLow().getId().equals(sender.getId())
                ? conversation.getParticipantHigh()
                : conversation.getParticipantLow();
        if (recipient == null || recipient.getEmail() == null || recipient.getEmail().isBlank()) {
            return;
        }
        // Un usuario nunca se envía correo a sí mismo (aunque el flujo
        // actual no permite auto-conversaciones, la guardia refuerza la
        // regla ante cambios futuros del dominio).
        if (recipient.getId().equals(sender.getId())) {
            return;
        }
        boolean wantsEmail = preferencesRepository.findById(recipient.getId())
                .map(p -> p.isNotifyByEmail())
                .orElse(false);
        if (!wantsEmail) {
            return;
        }

        String recipientEmail = recipient.getEmail();
        String recipientName = displayName(recipient);
        String senderName = displayName(sender);
        String preview = truncate(message.getContent(), 240);

        org.springframework.transaction.support.TransactionSynchronizationManager
                .registerSynchronization(
                        new org.springframework.transaction.support.TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                emailService.sendDirectMessageEmail(
                                        recipientEmail, recipientName, senderName, preview);
                            }
                        });
    }

    private static String displayName(User u) {
        String first = u.getFirstName() != null ? u.getFirstName() : "";
        String last = u.getLastName() != null ? u.getLastName() : "";
        String composed = (first + " " + last).trim();
        return composed.isEmpty() ? u.getUsername() : composed;
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max).trim() + "…";
    }

    /**
     * Marca como leídos todos los mensajes de la conversación cuyo
     * emisor no sea el usuario autenticado.
     */
    @Transactional
    public void markAsRead(Long conversationId) {
        User user = currentUserService.requireAuthenticatedUser();
        requireParticipant(conversationId, user);
        messageRepository.markConversationAsRead(conversationId, user.getId(),
                OffsetDateTime.now(ZoneOffset.UTC));
    }

    /**
     * Busca la conversación entre dos usuarios; si no existe, la crea
     * normalizando el par por id ascendente para respetar la restricción
     * de unicidad en la tabla.
     */
    private MessageConversation findOrCreate(User a, User b) {
        Long lowId = Math.min(a.getId(), b.getId());
        Long highId = Math.max(a.getId(), b.getId());
        return conversationRepository.findByParticipantLowIdAndParticipantHighId(lowId, highId)
                .orElseGet(() -> {
                    MessageConversation created = new MessageConversation();
                    created.setParticipantLow(lowId.equals(a.getId()) ? a : b);
                    created.setParticipantHigh(highId.equals(a.getId()) ? a : b);
                    return conversationRepository.save(created);
                });
    }

    private MessageConversation requireParticipant(Long conversationId, User user) {
        MessageConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "La conversación indicada no existe."));
        if (!conversation.includes(user.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "No tienes acceso a esta conversación.");
        }
        return conversation;
    }

    private MessageConversation requireVisibleConversation(Long conversationId, User user) {
        MessageConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND,
                        "La conversación indicada no existe."));
        boolean isAdmin = user.getRole() == tech.impulso.users.entity.Role.ADMINISTRADOR;
        if (!conversation.includes(user.getId()) && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN,
                    "No tienes acceso a esta conversación.");
        }
        return conversation;
    }

    private void enforceRateLimit(Long senderId) {
        OffsetDateTime windowStart = OffsetDateTime.now(ZoneOffset.UTC).minusHours(1);
        long recent = messageRepository.countBySenderIdAndSentAtAfter(senderId, windowStart);
        if (recent >= MAX_MESSAGES_PER_HOUR) {
            throw new BusinessException(HttpStatus.TOO_MANY_REQUESTS,
                    "Has alcanzado el límite de mensajes por hora.");
        }
    }

    private static String truncate(String content) {
        if (content == null) {
            return null;
        }
        String normalized = content.replaceAll("\\s+", " ").strip();
        return normalized.length() <= PREVIEW_LENGTH
                ? normalized
                : normalized.substring(0, PREVIEW_LENGTH - 1) + "…";
    }
}
