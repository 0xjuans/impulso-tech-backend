package tech.impulso.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.impulso.auth.entity.PasswordResetToken;
import tech.impulso.users.entity.User;

import java.util.Optional;

/**
 * Repositorio de persistencia para {@link PasswordResetToken}.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Localiza un token de recuperación por su valor.
     *
     * @param token valor recibido desde el enlace enviado al correo.
     * @return el token si existe.
     */
    Optional<PasswordResetToken> findByToken(String token);

    /**
     * Elimina los tokens pendientes de recuperación de un usuario. Se
     * utiliza cuando se genera uno nuevo para invalidar los anteriores.
     *
     * @param user usuario cuyos tokens deben eliminarse.
     */
    @Modifying
    @Query("delete from PasswordResetToken t where t.user = :user")
    void deleteByUser(@Param("user") User user);
}
