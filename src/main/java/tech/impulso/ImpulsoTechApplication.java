package tech.impulso;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada de la aplicación backend de Impulso Tech.
 *
 * <p>Arranca el contexto de Spring Boot, habilita el auto-configuración
 * y publica todos los módulos funcionales de la plataforma bajo el
 * paquete raíz {@code tech.impulso}.</p>
 *
 * <p>Los módulos (auth, users, courses, gamification, etc.) se descubren
 * automáticamente mediante el escaneo de componentes de Spring, siempre
 * que se encuentren dentro de este paquete raíz.</p>
 */
@SpringBootApplication
public class ImpulsoTechApplication {

    /**
     * Método principal invocado por la JVM para iniciar la aplicación.
     *
     * @param args argumentos de línea de comandos entregados por el sistema
     *             operativo o por el orquestador de contenedores.
     */
    public static void main(String[] args) {
        SpringApplication.run(ImpulsoTechApplication.class, args);
    }
}
