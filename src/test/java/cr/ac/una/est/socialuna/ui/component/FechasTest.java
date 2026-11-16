package cr.ac.una.est.socialuna.ui.component;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FechasTest {

    //Prueba los cortes de la fecha relativa con un momento fijo.
    @Test void fechaRelativaCambiaDeUnidadSegunElTiempo() {
        long ahora = 1_000_000_000_000L;

        assertEquals("hace un momento", Fechas.relativa(ahora - 59_000, ahora));
        assertEquals("hace 2 min", Fechas.relativa(ahora - 2 * 60_000, ahora));
        assertEquals("hace 3 h", Fechas.relativa(ahora - 3 * 3_600_000, ahora));
        assertEquals("hace 2 d", Fechas.relativa(ahora - 2 * 86_400_000L, ahora));
    }
}
