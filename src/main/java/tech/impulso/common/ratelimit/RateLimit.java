package tech.impulso.common.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca un endpoint como sujeto a limitación de tasa (RF-034 / §34).
 *
 * <p>Se aplica sobre métodos de controladores REST. Cada ejecución
 * consulta Redis para determinar si el cliente (usuario autenticado o
 * dirección IP cuando no hay sesión) superó el umbral configurado
 * dentro de la ventana temporal indicada.</p>
 *
 * @param bucket        identificador semántico del contador. Los
 *                      endpoints que comparten bucket comparten
 *                      contador; por ejemplo, todos los intentos de
 *                      inicio de sesión pueden agruparse bajo
 *                      {@code auth-login}.
 * @param limit         número máximo de solicitudes permitidas al
 *                      mismo cliente dentro de la ventana.
 * @param windowSeconds duración de la ventana temporal en segundos.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String bucket();

    int limit();

    int windowSeconds();
}
