package tech.impulso.auth.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tech.impulso.users.entity.User;
import tech.impulso.users.entity.UserStatus;
import tech.impulso.users.repository.UserRepository;

import java.util.List;

/**
 * Implementación de {@link UserDetailsService} basada en la entidad
 * {@link User} de Impulso Tech.
 *
 * <p>Carga al usuario por su correo electrónico y lo transforma en un
 * {@link UserDetails} utilizable por Spring Security. Aplica las reglas
 * de estado de cuenta definidas en el RF-002: un usuario pendiente de
 * verificación o desactivado no puede autenticarse aunque las
 * credenciales sean correctas.</p>
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Carga el usuario asociado al correo electrónico suministrado.
     *
     * @param email correo electrónico utilizado como identificador de acceso.
     * @return el detalle del usuario compatible con Spring Security.
     * @throws UsernameNotFoundException cuando no existe una cuenta con el
     *         correo indicado.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales inválidas."));

        boolean enabled = user.getStatus() == UserStatus.ACTIVA;

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPasswordHash() == null ? "" : user.getPasswordHash(),
                enabled,
                true,
                true,
                true,
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
        );
    }
}
