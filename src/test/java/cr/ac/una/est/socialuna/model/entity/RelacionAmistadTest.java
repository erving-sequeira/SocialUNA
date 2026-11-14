package cr.ac.una.est.socialuna.model.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RelacionAmistadTest {
    //Prueba que la amistad conserve el ID, los dos usuarios y la fecha.
    @Test void conservaIdParticipantesYFecha() {
        RelacionAmistad r = new RelacionAmistad("r1", "u1", "u2", 1730000000000L);
        assertEquals("r1", r.getId()); assertEquals("u1", r.getUsuarioAId());
        assertEquals("u2", r.getUsuarioBId()); assertEquals(1730000000000L, r.getCreadoEn());
    }
    //Prueba que los dos usuarios se reconozcan como parte de la amistad.
    @Test void reconoceAmbosExtremosDeLaAmistad() {
        RelacionAmistad r = new RelacionAmistad("r1", "u1", "u2", 0);
        assertTrue(r.involucra("u1")); assertTrue(r.involucra("u2"));
        assertFalse(r.involucra("u3")); assertFalse(r.involucra(null)); assertFalse(r.involucra(""));
    }
    //Prueba que el ID de cualquiera de los dos permita encontrar al otro.
    @Test void obtieneAmigoEnAmbasDirecciones() {
        RelacionAmistad r = new RelacionAmistad("r1", "u1", "u2", 0);
        assertEquals("u2", r.obtenerOtroUsuario("u1")); assertEquals("u1", r.obtenerOtroUsuario("u2"));
    }
    //Prueba que un ID ajeno no sirva para pedir el otro extremo de la amistad.
    @Test void rechazaPedirElOtroExtremoParaUnUsuarioAjeno() {
        RelacionAmistad r = new RelacionAmistad("r1", "u1", "u2", 0);
        for (String ajeno : new String[]{"u3", "", null}) {
            assertThrows(IllegalArgumentException.class, () -> r.obtenerOtroUsuario(ajeno));
        }
    }
    //Prueba que el mismo usuario no se pueda agregar como amigo ni cambiando los espacios.
    @Test void rechazaAutoamistadInclusoConEspaciosExteriores() {
        assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1", "u1", "u1", 0));
        assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1", " u1 ", "u1", 0));
    }
    //Prueba que la amistad falle cuando falta alguno de los IDs.
    @Test void rechazaIdsAusentes() {
        for (String invalido : new String[]{null, "", "  "}) {
            assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad(invalido,"u1","u2",0));
            assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1",invalido,"u2",0));
            assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1","u1",invalido,0));
        }
    }
    //Prueba que la fecha cero sea válida y las fechas negativas den error.
    @Test void rechazaFechaNegativaPeroAceptaElInicioDeEpoca() {
        assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1","u1","u2",-1));
        assertEquals(0, new RelacionAmistad("r1","u1","u2",0).getCreadoEn());
    }
    //Prueba que cambiar el orden de los dos usuarios no cambie las consultas.
    @Test void elOrdenDeLosParticipantesNoCambiaLasConsultas() {
        RelacionAmistad ab = new RelacionAmistad("r1","u1","u2",1);
        RelacionAmistad ba = new RelacionAmistad("r2","u2","u1",2);
        assertEquals(ab.obtenerOtroUsuario("u1"),ba.obtenerOtroUsuario("u1"));
        assertEquals(ab.obtenerOtroUsuario("u2"),ba.obtenerOtroUsuario("u2"));
    }
}
