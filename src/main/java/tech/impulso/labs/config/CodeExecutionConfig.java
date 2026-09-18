package tech.impulso.labs.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita el binding de {@link CodeExecutionProperties} para permitir
 * seleccionar el modo del sandbox y sus límites desde
 * {@code application.yml} o variables de entorno.
 */
@Configuration
@EnableConfigurationProperties(CodeExecutionProperties.class)
public class CodeExecutionConfig {
}
