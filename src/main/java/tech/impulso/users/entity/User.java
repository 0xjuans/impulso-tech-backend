package tech.impulso.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Entidad persistente que representa a un usuario de Impulso Tech.
 *
 * <p>Contiene la información básica del perfil, el rol asignado, el estado
 * de la cuenta y las marcas de tiempo necesarias para auditar el ciclo de
 * vida del usuario. Las contraseñas se almacenan siempre cifradas mediante
 * BCrypt; nunca se guarda el valor en texto plano.</p>
 *
 * <p>Los cambios en el modelo de datos se gestionan exclusivamente mediante
 * migraciones de Flyway; esta clase debe reflejar el esquema definido en
 * {@code V2__users_and_auth.sql}.</p>
 */
@Entity
@Table(name = "users")
public class User {

    /** Identificador interno del usuario asignado por la base de datos. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Correo electrónico único del usuario, utilizado como identificador
     * de acceso al iniciar sesión y para las comunicaciones
     * transaccionales.
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Nombre de usuario público. Se utiliza en el perfil, la comunidad y
     * los rankings. Debe ser único en toda la plataforma.
     */
    @Column(name = "username", nullable = false, unique = true, length = 60)
    private String username;

    /**
     * Hash BCrypt de la contraseña. Es nulo cuando el usuario se autentica
     * exclusivamente mediante proveedores externos como Google OAuth.
     */
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    /** Nombre real del usuario. */
    @Column(name = "first_name", nullable = false, length = 80)
    private String firstName;

    /** Apellido real del usuario. */
    @Column(name = "last_name", nullable = false, length = 80)
    private String lastName;

    /**
     * URL pública de la foto de perfil almacenada en Cloudflare R2. Es
     * opcional; cuando no está definida se mostrará una imagen genérica.
     */
    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    /** Rol funcional asignado al usuario. */
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    private Role role;

    /** Estado del ciclo de vida de la cuenta. */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private UserStatus status;

    /**
     * Indica si el usuario acepta aparecer en los rankings públicos
     * (RF-021). Cuando es {@code false}, sus estadísticas se siguen
     * registrando pero no se exponen en las listas de clasificación.
     */
    @Column(name = "show_in_ranking", nullable = false)
    private boolean showInRanking = true;

    /**
     * Fecha y hora en que el usuario confirmó su correo electrónico. Es
     * nulo mientras la cuenta se encuentre en estado
     * {@link UserStatus#PENDIENTE_VERIFICACION}.
     */
    @Column(name = "email_verified_at")
    private OffsetDateTime emailVerifiedAt;

    /** Marca de tiempo del último inicio de sesión exitoso. */
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;

    /** Fecha de creación del registro en la base de datos. */
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Fecha de la última modificación del registro. */
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * Inicializa las marcas de tiempo al momento de insertar por primera
     * vez la entidad.
     */
    @PrePersist
    void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * Actualiza la marca de tiempo de modificación cada vez que se
     * persiste un cambio sobre la entidad.
     */
    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProfilePhotoUrl() {
        return profilePhotoUrl;
    }

    public void setProfilePhotoUrl(String profilePhotoUrl) {
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public boolean isShowInRanking() {
        return showInRanking;
    }

    public void setShowInRanking(boolean showInRanking) {
        this.showInRanking = showInRanking;
    }

    public OffsetDateTime getEmailVerifiedAt() {
        return emailVerifiedAt;
    }

    public void setEmailVerifiedAt(OffsetDateTime emailVerifiedAt) {
        this.emailVerifiedAt = emailVerifiedAt;
    }

    public OffsetDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(OffsetDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
