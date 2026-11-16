package tech.impulso.ai.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pruebas de la utilidad {@link MascotService#tailWindow(List, int)},
 * responsable de recortar el historial enviado al proveedor de IA para
 * optimizar el uso de tokens.
 */
class MascotServiceTailWindowTest {

    @Test
    void devuelveMismaListaCuandoTamañoEsMenorOIgualAlMáximo() {
        List<Integer> input = List.of(1, 2, 3);
        assertSame(input, MascotService.tailWindow(input, 5));
        assertSame(input, MascotService.tailWindow(input, 3));
    }

    @Test
    void devuelveSoloLaColaCuandoSuperaElMáximo() {
        List<Integer> input = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> tail = MascotService.tailWindow(input, 4);
        assertEquals(List.of(7, 8, 9, 10), tail);
    }

    @Test
    void devuelveMismaListaCuandoMaxSizeEsCeroONegativo() {
        List<Integer> input = List.of(1, 2, 3);
        assertSame(input, MascotService.tailWindow(input, 0));
        assertSame(input, MascotService.tailWindow(input, -1));
    }

    @Test
    void manejaListaVaciaSinFallar() {
        assertEquals(0, MascotService.tailWindow(List.of(), 5).size());
    }
}
