package tech.impulso.mascotcustomization.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.impulso.mascotcustomization.entity.MascotCustomization;

/**
 * Repositorio de persistencia para {@link MascotCustomization}.
 *
 * <p>La clave primaria coincide con el identificador del usuario, de
 * modo que {@code findById} y {@code deleteById} operan por
 * {@code userId}.</p>
 */
@Repository
public interface MascotCustomizationRepository extends JpaRepository<MascotCustomization, Long> {
}
