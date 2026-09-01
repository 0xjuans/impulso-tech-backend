package tech.impulso.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.auth.entity.EmailVerificationToken;
import tech.impulso.users.entity.User;

import java.util.Optional;

/**
 * Repositorio de persistencia para {@link EmailVerificationToken}.
 */
@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /**
     * Localiza un token de verificación por su valor.
     *
     * @param token valor recibido desde el enlace enviado al correo.
     * @return el token si existe.
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Elimina los tokens pendientes de un usuario. Se utiliza cuando se
     * genera uno nuevo, para evitar acumulaciones y garantizar que solo
     * exista un enlace activo a la vez.
     *
     * @param user usuario cuyos tokens deben eliminarse.
     */
    @Modifying
    @Query("delete from EmailVerificationToken t where t.user = :user")
    void deleteByUser(@Param("user") User user);
}
