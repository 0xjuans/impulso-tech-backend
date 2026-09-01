package tech.impulso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tech.impulso.auth.filter.JwtAuthenticationFilter;

/**
 * Configuración base de seguridad de la aplicación.
 *
 * <p>Define el comportamiento stateless de la API, delega la
 * autenticación de las peticiones al {@link JwtAuthenticationFilter}
 * y protege todos los endpoints por defecto, exceptuando los recursos
 * públicos necesarios (documentación de la API, health check y flujos
 * de autenticación).</p>
 *
 * <p>La autorización granular por rol se aplica de forma declarativa a
 * nivel de controlador mediante anotaciones {@code @PreAuthorize} en los
 * módulos correspondientes.</p>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/actuator/health",
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/google",
            "/api/auth/verify",
            "/api/auth/verify/resend",
            "/api/auth/password/forgot",
            "/api/auth/password/reset"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * Define la cadena de filtros de seguridad aplicada a las peticiones
     * HTTP.
     *
     * @param http constructor de la configuración de seguridad HTTP.
     * @return la cadena de filtros de seguridad resultante.
     * @throws Exception si ocurre un error al construir la configuración.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Expone el {@link AuthenticationManager} configurado por Spring
     * Security para que pueda inyectarse en los flujos personalizados
     * (por ejemplo, integraciones con OAuth 2.0 en fases posteriores).
     *
     * @param configuration configuración de autenticación de Spring.
     * @return el gestor de autenticación resultante.
     * @throws Exception si Spring no puede construirlo.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /**
     * Codificador de contraseñas utilizado por la aplicación.
     *
     * <p>Se emplea BCrypt para almacenar las contraseñas de los usuarios,
     * evitando cualquier persistencia en texto plano y aplicando un salt
     * único por contraseña.</p>
     *
     * @return la instancia compartida del codificador de contraseñas.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
