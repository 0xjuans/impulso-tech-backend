package tech.impulso.ai.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita el binding de {@link AiProperties} para poder seleccionar y
 * configurar el proveedor de IA desde {@code application.yml} o
 * variables de entorno.
 */
@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfig {
}
