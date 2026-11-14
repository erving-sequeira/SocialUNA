package cr.ac.una.est.socialuna.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Verifica el registro y resolución de fábricas de controladores (sin toolkit JavaFX). */
class NavegadorVistasTest {

    @Test
    void usaLaFabricaRegistradaParaCrearElControlador() {
        NavegadorVistas navegador = new NavegadorVistas(null);
        navegador.registrarFabrica(String.class, () -> "controlador-de-prueba");

        assertEquals("controlador-de-prueba", navegador.crearControlador(String.class));
    }

    @Test
    void fallaConMensajeClaroSiNoHayFabricaRegistrada() {
        NavegadorVistas navegador = new NavegadorVistas(null);

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> navegador.crearControlador(Object.class));

        assertEquals(true, error.getMessage().contains("registrarFabrica"));
    }
}
