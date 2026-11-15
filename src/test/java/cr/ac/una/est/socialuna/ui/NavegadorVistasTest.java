package cr.ac.una.est.socialuna.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class NavegadorVistasTest {

    //Prueba que el navegador use la fábrica registrada para crear su controlador.
    @Test
    void usaLaFabricaRegistradaParaCrearElControlador() {
        NavegadorVistas navegador = new NavegadorVistas(null);
        navegador.registrarFabrica(String.class, () -> "controlador-de-prueba");

        assertEquals("controlador-de-prueba", navegador.crearControlador(String.class));
    }

    //Prueba que pedir un controlador sin fábrica dé un error que explique qué falta.
    @Test
    void fallaConMensajeClaroSiNoHayFabricaRegistrada() {
        NavegadorVistas navegador = new NavegadorVistas(null);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> navegador.crearControlador(Object.class));

        assertEquals(true, error.getMessage().contains("registrarFabrica"));
    }

    //Prueba que la fábrica que devuelve null dé un error.
    @Test
    void informaSiLaFabricaDevuelveNull() {
        var navegador = new NavegadorVistas(null);
        navegador.registrarFabrica(String.class, () -> null);
        assertThrows(IllegalStateException.class, () -> navegador.crearControlador(String.class));
    }
}
