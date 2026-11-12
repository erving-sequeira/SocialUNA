package cr.ac.una.est.socialuna.model.entity;

import cr.ac.una.est.socialuna.logic.model.entity.RelacionAmistad;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RelacionAmistadTest {
    @Test void conservaIdParticipantesYFecha() {
        RelacionAmistad r = new RelacionAmistad("r1", "u1", "u2", 1730000000000L);
        assertEquals("r1", r.getId()); assertEquals("u1", r.getUsuarioAId());
        assertEquals("u2", r.getUsuarioBId()); assertEquals(1730000000000L, r.getCreadoEn());
    }
    @Test void reconoceAmbosExtremosDeLaAmistad() {
        RelacionAmistad r = new RelacionAmistad("r1", "u1", "u2", 0);
        assertTrue(r.involucra("u1")); assertTrue(r.involucra("u2"));
        assertFalse(r.involucra("u3")); assertFalse(r.involucra(null)); assertFalse(r.involucra(""));
    }
    @Test void obtieneAmigoEnAmbasDirecciones() {
        RelacionAmistad r = new RelacionAmistad("r1", "u1", "u2", 0);
        assertEquals("u2", r.obtenerOtroUsuario("u1")); assertEquals("u1", r.obtenerOtroUsuario("u2"));
    }
    @Test void rechazaPedirElOtroExtremoParaUnUsuarioAjeno() {
        RelacionAmistad r = new RelacionAmistad("r1", "u1", "u2", 0);
        for (String ajeno : new String[]{"u3", "", null}) {
            assertThrows(IllegalArgumentException.class, () -> r.obtenerOtroUsuario(ajeno));
        }
    }
    @Test void rechazaAutoamistadInclusoConEspaciosExteriores() {
        assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1", "u1", "u1", 0));
        assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1", " u1 ", "u1", 0));
    }
    @Test void rechazaIdsAusentes() {
        for (String invalido : new String[]{null, "", "  "}) {
            assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad(invalido,"u1","u2",0));
            assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1",invalido,"u2",0));
            assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1","u1",invalido,0));
        }
    }
    @Test void rechazaFechaNegativaPeroAceptaElInicioDeEpoca() {
        assertThrows(IllegalArgumentException.class, () -> new RelacionAmistad("r1","u1","u2",-1));
        assertEquals(0, new RelacionAmistad("r1","u1","u2",0).getCreadoEn());
    }
    @Test void elOrdenDeLosParticipantesNoCambiaLasConsultas() {
        RelacionAmistad ab = new RelacionAmistad("r1","u1","u2",1);
        RelacionAmistad ba = new RelacionAmistad("r2","u2","u1",2);
        assertEquals(ab.obtenerOtroUsuario("u1"),ba.obtenerOtroUsuario("u1"));
        assertEquals(ab.obtenerOtroUsuario("u2"),ba.obtenerOtroUsuario("u2"));
    }
}
