package tech.impulso.legal.entity;

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
 * Registro de la aceptación de una versión específica de un documento
 * legal por parte de un usuario (RF-062).
 *
 * <p>La restricción {@code UNIQUE(user_id, document_id)} en base de
 * datos impide aceptaciones duplicadas.</p>
 */
@Entity
@Table(name = "user_legal_acceptances")
public class UserLegalAcceptance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Usuario que aceptó el documento. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** Versión del documento aceptada. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private LegalDocument document;

    /** Momento en que se registró la aceptación. */
    @Column(name = "accepted_at", nullable = false, updatable = false)
    private OffsetDateTime acceptedAt;

    /** Dirección IP desde la cual el usuario aceptó, cuando esté disponible. */
    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @PrePersist
    void onCreate() {
        if (this.acceptedAt == null) {
            this.acceptedAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LegalDocument getDocument() {
        return document;
    }

    public void setDocument(LegalDocument document) {
        this.document = document;
    }

    public OffsetDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
