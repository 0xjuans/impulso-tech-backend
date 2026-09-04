package tech.impulso.messaging.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import tech.impulso.users.entity.User;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Conversación directa entre dos usuarios (RF-061).
 *
 * <p>Los participantes se almacenan siempre en orden ascendente por id
 * ({@code participantLow < participantHigh}). Esta convención junto con
 * el índice único permite reconocer la conversación existente entre dos
 * usuarios independientemente del orden en que la abran.</p>
 */
@Entity
@Table(name = "message_conversations")
public class MessageConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Participante con el identificador más bajo. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_low_id", nullable = false)
    private User participantLow;

    /** Participante con el identificador más alto. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_high_id", nullable = false)
    private User participantHigh;

    /** Fecha de creación de la conversación. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Fecha del último mensaje intercambiado. */
    @Column(name = "last_message_at", nullable = false)
    private OffsetDateTime lastMessageAt;

    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.lastMessageAt = now;
    }

    public Long getId() { return id; }
    public User getParticipantLow() { return participantLow; }
    public void setParticipantLow(User participantLow) { this.participantLow = participantLow; }
    public User getParticipantHigh() { return participantHigh; }
    public void setParticipantHigh(User participantHigh) { this.participantHigh = participantHigh; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(OffsetDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }

    /**
     * Indica si el usuario con el id suministrado participa en la
     * conversación.
     */
    public boolean includes(Long userId) {
        return participantLow.getId().equals(userId) || participantHigh.getId().equals(userId);
    }

    /**
     * Devuelve el otro participante de la conversación, distinto al
     * usuario suministrado.
     */
    public User otherParticipant(Long userId) {
        return participantLow.getId().equals(userId) ? participantHigh : participantLow;
    }
}
