package tech.impulso.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración global de la documentación OpenAPI / Swagger UI.
 *
 * <p>Define la información pública de la API de Impulso Tech (nombre,
 * versión, contacto, licencia). La interfaz de Swagger UI queda expuesta
 * de forma predeterminada en {@code /swagger-ui.html} y el esquema
 * OpenAPI en {@code /v3/api-docs}.</p>
 *
 * <p>La documentación se habilita principalmente en los entornos de
 * desarrollo y pruebas. En producción podrá restringirse o deshabilitarse
 * según las políticas de seguridad definidas por la plataforma.</p>
 */
@Configuration
public class OpenApiConfig {

    /**
     * Construye el objeto {@link OpenAPI} con la información base de la API.
     *
     * @return la configuración de OpenAPI utilizada por Springdoc para
     *         generar el esquema y la interfaz de Swagger UI.
     */
    @Bean
    public OpenAPI impulsoTechOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Impulso Tech API")
                        .description("API REST del backend de Impulso Tech, plataforma web para el aprendizaje de programación.")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("Equipo Impulso Tech")
                                .url("https://github.com/0xjuans/impulso-tech-backend"))
                        .license(new License()
                                .name("Uso interno")
                                .url("https://github.com/0xjuans/impulso-tech-backend")));
    }
}
