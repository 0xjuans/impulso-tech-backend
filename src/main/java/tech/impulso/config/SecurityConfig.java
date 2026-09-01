package tech.impulso.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración base de seguridad de la aplicación.
 *
 * <p>Esta configuración inicial deja abiertos únicamente los recursos
 * públicos necesarios durante el bootstrap del proyecto (documentación
 * de la API, health check y endpoints de autenticación). El resto de los
 * endpoints exigen autenticación. Los filtros de JWT, la integración con
 * Google OAuth 2.0 y la autorización granular por rol se incorporarán en
 * el módulo {@code auth} en etapas posteriores.</p>
 *
 * <p>La aplicación opera de forma stateless para adecuarse al modelo de
 * API REST y evitar la creación de sesiones HTTP en el servidor.</p>
 */
@Configuration
public class SecurityConfig {

    /**
     * Rutas públicas que no requieren autenticación durante esta etapa
     * del proyecto. Se limitan a la documentación de la API, al monitoreo
     * básico y a los endpoints de autenticación que se implementarán a
     * continuación.
     */
    private static final String[] PUBLIC_ENDPOINTS = {
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/actuator/health",
            "/api/auth/**"
    };

    /**
     * Define la cadena de filtros de seguridad HTTP aplicada a todas las
     * peticiones. Deshabilita CSRF por tratarse de una API REST stateless,
     * abre los recursos públicos definidos y exige autenticación para el
     * resto de los endpoints.
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
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated());

        return http.build();
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
