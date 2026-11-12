package cr.ac.una.est.socialuna.model.entity;

import cr.ac.una.est.socialuna.logic.model.entity.Carrera;
import cr.ac.una.est.socialuna.logic.model.entity.SedeCampus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CatalogosTest {
    @Test void carreraConservaIdNombreYEstado() {
        Carrera c = new Carrera("c1", "Informática", true);
        assertEquals("c1",c.getId()); assertEquals("Informática",c.getNombre()); assertTrue(c.isActiva());
        assertFalse(new Carrera("c2","Administración",false).isActiva());
    }
    @Test void sedeConservaTodosSusDatosInclusoAliasOpcional() {
        SedeCampus s = new SedeCampus("s1","Interuniversitaria de Alajuela","SIUA","Campus",true);
        assertEquals("s1",s.getId()); assertEquals("Interuniversitaria de Alajuela",s.getNombre());
        assertEquals("SIUA",s.getAlias()); assertEquals("Campus",s.getTipo()); assertTrue(s.isActiva());
        SedeCampus sinAlias = new SedeCampus("s2","Benjamín Núñez",null,"Campus",false);
        assertNull(sinAlias.getAlias()); assertFalse(sinAlias.isActiva());
    }
    @Test void catalogosRechazanIdentidadesONombresVacios() {
        for(String invalido:new String[]{null,"","  "}) {
            assertThrows(IllegalArgumentException.class,()->new Carrera(invalido,"Informática",true));
            assertThrows(IllegalArgumentException.class,()->new Carrera("c1",invalido,true));
            assertThrows(IllegalArgumentException.class,()->new SedeCampus(invalido,"SIUA",null,"Campus",true));
            assertThrows(IllegalArgumentException.class,()->new SedeCampus("s1",invalido,null,"Campus",true));
        }
    }
}
