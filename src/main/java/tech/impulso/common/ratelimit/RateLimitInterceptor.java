package tech.impulso.common.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor que hace cumplir los límites declarados con
 * {@link RateLimit} sobre los handlers REST.
 *
 * <p>Extrae la clave del cliente a partir del usuario autenticado —
 * cuando existe — y en su defecto de la dirección IP. Cuando el
 * cliente supera el umbral responde {@code 429 Too Many Requests} con
 * el cuerpo estándar de la API y una cabecera {@code Retry-After}.
 * También añade cabeceras informativas
 * {@code X-RateLimit-*} en toda respuesta que atraviese un endpoint
 * limitado, para que el frontend pueda mostrar mensajes anticipados
 * (por ejemplo, "quedan 2 intentos").</p>
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final String BODY_TEMPLATE = """
            {"timestamp":"%s","status":429,"error":"Too Many Requests",\
            "message":"Has excedido el límite de solicitudes. Intenta nuevamente en unos segundos.",\
            "path":"%s"}""";

    private final RateLimitService service;
    private final RateLimitProperties properties;

    public RateLimitInterceptor(RateLimitService service, RateLimitProperties properties) {
        this.service = service;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        if (!properties.isEnabled()) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        RateLimit annotation = handlerMethod.getMethodAnnotation(RateLimit.class);
        if (annotation == null) {
            return true;
        }

        String clientKey = resolveClientKey(request);
        RateLimitService.Decision decision = service.check(
                annotation.bucket(),
                clientKey,
                annotation.limit(),
                annotation.windowSeconds()
        );

        response.setHeader("X-RateLimit-Limit", String.valueOf(annotation.limit()));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(decision.remaining()));
        response.setHeader("X-RateLimit-Reset", String.valueOf(decision.resetInSeconds()));

        if (!decision.allowed()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", String.valueOf(decision.resetInSeconds()));
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(BODY_TEMPLATE.formatted(
                    java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC),
                    request.getRequestURI()
            ));
            return false;
        }
        return true;
    }

    /**
     * Devuelve la clave del cliente para agrupar el contador. Se
     * prefiere el usuario autenticado; sin sesión se cae a la IP
     * respetando {@code X-Forwarded-For} cuando el proxy de Fly lo
     * añade.
     */
    private String resolveClientKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equalsIgnoreCase(auth.getName())) {
            return "u:" + auth.getName();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
        return "ip:" + (ip == null ? "unknown" : ip);
    }
}
