package tech.impulso;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Prueba de humo que verifica que el contexto de Spring Boot arranca
 * correctamente con la configuración actual del proyecto.
 *
 * <p>Sirve como red de seguridad inicial: cualquier cambio que rompa la
 * inicialización del contexto (beans mal configurados, dependencias
 * inconsistentes, migraciones inválidas) será detectado por esta prueba.</p>
 */
@SpringBootTest
@ActiveProfiles("test")
class ImpulsoTechApplicationTests {

    /**
     * Verifica que el contexto de la aplicación se levanta sin errores.
     */
    @Test
    void contextLoads() {
    }
}
