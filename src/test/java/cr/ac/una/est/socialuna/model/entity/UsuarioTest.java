package cr.ac.una.est.socialuna.model.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {
    private static final String HASH = "a".repeat(64);

    @Test void conservaIdentidadYReferenciasDelConstructor() {
        Usuario u = new Usuario("u1", "Ana María", "ana", HASH, "c1", "s1");
        assertAll(() -> assertEquals("u1", u.getId()), () -> assertEquals("Ana María", u.getNombre()),
                () -> assertEquals("ana", u.getNombreUsuario()), () -> assertEquals(HASH, u.getPasswordHash()),
                () -> assertEquals("c1", u.getCarreraId()), () -> assertEquals("s1", u.getSedeId()));
    }

    @Test void permiteRegistrarSinDatosAcademicos() {
        Usuario u = new Usuario("u1", "Ana", "ana", HASH, null, "  ");
        assertNull(u.getCarreraId()); assertNull(u.getSedeId());
    }

    @Test void modificaYEliminaSeleccionAcademicaSinCambiarIdentidad() {
        Usuario u = new Usuario("u1", "Ana", "ana", HASH, null, null);
        u.setCarreraId(" c2 "); u.setSedeId(" s2 ");
        assertEquals("c2", u.getCarreraId()); assertEquals("s2", u.getSedeId());
        u.setCarreraId(""); u.setSedeId(null);
        assertNull(u.getCarreraId()); assertNull(u.getSedeId());
        assertEquals("u1", u.getId()); assertEquals(HASH, u.getPasswordHash());
    }

    @Test void rechazaCamposObligatoriosNulosOVacios() {
        for (String invalido : new String[]{null, "", "  ", "\t"}) {
            assertThrows(IllegalArgumentException.class, () -> new Usuario(invalido,"Ana","ana",HASH,null,null));
            assertThrows(IllegalArgumentException.class, () -> new Usuario("u1",invalido,"ana",HASH,null,null));
            assertThrows(IllegalArgumentException.class, () -> new Usuario("u1","Ana",invalido,HASH,null,null));
            assertThrows(IllegalArgumentException.class, () -> new Usuario("u1","Ana","ana",invalido,null,null));
        }
    }

    @Test void conservaElIdEntreReconstruccionesSinGenerarOtro() {
        Usuario u = new Usuario("id-persistido", " Ana ", " ana ", HASH, "c1", "s1");
        Usuario copia = new Usuario(u.getId(), u.getNombre(), u.getNombreUsuario(), u.getPasswordHash(), u.getCarreraId(), u.getSedeId());
        assertEquals("id-persistido", copia.getId()); assertEquals("Ana", copia.getNombre());
        assertEquals("ana", copia.getNombreUsuario());
    }
}
