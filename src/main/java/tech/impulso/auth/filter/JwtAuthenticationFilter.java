package tech.impulso.auth.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.impulso.auth.service.JwtService;
import tech.impulso.auth.service.TokenBlacklistService;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que autentica cada petición HTTP a partir del token JWT enviado
 * en el encabezado {@code Authorization}.
 *
 * <p>Ejecuta las siguientes validaciones antes de establecer la
 * autenticación en el {@link SecurityContextHolder}:</p>
 *
 * <ol>
 *   <li>El encabezado contiene un valor con el prefijo {@code Bearer}.</li>
 *   <li>El token es firmado por la plataforma y no ha expirado.</li>
 *   <li>El identificador del token no aparece en la lista negra de
 *       tokens revocados (cierre de sesión).</li>
 * </ol>
 *
 * <p>Si alguna validación falla, la petición continúa sin autenticación
 * y Spring Security responderá con 401 o 403 según corresponda.</p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length());
        try {
            Claims claims = jwtService.parseToken(token);
            if (tokenBlacklistService.isBlacklisted(claims.getId())) {
                filterChain.doFilter(request, response);
                return;
            }

            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Guardamos los claims como atributo para que el controlador de
            // logout pueda invalidarlos por identificador sin volver a
            // parsear el token.
            request.setAttribute("jwtClaims", claims);
        } catch (JwtException ignored) {
            // Un token inválido o expirado se trata como ausencia de
            // autenticación: no rompemos la petición para que los recursos
            // públicos sigan siendo accesibles.
        }

        filterChain.doFilter(request, response);
    }
}
