package tech.impulso.certificates.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.certificates.entity.Certificate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositorio de persistencia para {@link Certificate}.
 */
@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {

    /**
     * Devuelve los certificados del usuario indicado, más recientes primero.
     */
    List<Certificate> findByUserIdOrderByIssuedAtDesc(Long userId);

    /**
     * Busca un certificado por su código público de verificación.
     */
    Optional<Certificate> findByVerificationCode(UUID verificationCode);

    /**
     * Verifica si ya existe un certificado emitido para la combinación
     * usuario/curso, para hacer idempotente la emisión.
     */
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
}
