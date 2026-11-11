package cr.ac.una.est.socialuna.model.graph;

import cr.ac.una.est.socialuna.logic.model.graph.GrafoAmigos;
import cr.ac.una.est.socialuna.logic.model.graph.VerticeAmigos;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GrafoAmigosTest {
    private final GrafoAmigos grafo = new GrafoAmigos();

    @Test void conexionEsReciprocaSinDuplicarAdyacencias() {
        grafo.agregarUsuario("u1"); grafo.agregarUsuario("u2");
        grafo.conectar("u1", "u2");
        grafo.conectar("u1", "u2"); grafo.conectar("u2", "u1");
        assertTrue(grafo.sonAmigos("u1", "u2")); assertTrue(grafo.sonAmigos("u2", "u1"));
        assertEquals(1, grafo.obtenerAmigos("u1").size());
        assertEquals(1, grafo.obtenerAmigos("u2").size());
    }

    @Test void agregarUsuarioRepetidoNoCreaVerticeDuplicado() {
        grafo.agregarUsuario("u1"); grafo.agregarUsuario("u1"); grafo.agregarUsuario(" u1 ");
        grafo.agregarUsuario("u2"); grafo.conectar("u1", "u2");
        assertEquals(1, grafo.obtenerAmigos("u1").size());
    }

    @Test void desconectarRetiraAmbasAdyacencias() {
        grafo.agregarUsuario("u1"); grafo.agregarUsuario("u2");
        grafo.conectar("u1", "u2");
        grafo.desconectar("u1", "u2");
        assertFalse(grafo.sonAmigos("u1", "u2")); assertFalse(grafo.sonAmigos("u2", "u1"));
        assertTrue(grafo.obtenerAmigos("u1").isEmpty());
        grafo.desconectar("u1", "u2");
    }

    @Test void usuarioAisladoNoEsAmigoDeNadie() {
        grafo.agregarUsuario("u1");
        assertFalse(grafo.sonAmigos("u1", "u2"));
        assertTrue(grafo.obtenerAmigos("u1").isEmpty());
        assertTrue(grafo.obtenerAmigos("u2").isEmpty());
        assertTrue(grafo.obtenerAmigos(null).isEmpty());
        assertFalse(grafo.sonAmigos(null, "u1"));
    }

    @Test void rechazaAutoamistad() {
        grafo.agregarUsuario("u1");
        assertThrows(IllegalArgumentException.class, () -> grafo.conectar("u1", "u1"));
        assertThrows(IllegalArgumentException.class, () -> grafo.desconectar("u1", "u1"));
        assertThrows(IllegalArgumentException.class, () -> grafo.conectar(null, "u1"));
    }

    @Test void exigeLosVerticesParaConectarYDesconectar() {
        assertThrows(IllegalArgumentException.class, () -> grafo.conectar("u1", "u2"));
        grafo.agregarUsuario("u1");
        assertThrows(IllegalArgumentException.class, () -> grafo.conectar("u1", "u2"));
        assertThrows(IllegalArgumentException.class, () -> grafo.desconectar("u1", "u2"));
        assertThrows(IllegalArgumentException.class, () -> grafo.agregarUsuario("  "));
    }

    @Test void vaciarDejaElGrafoListoParaReconstruir() {
        grafo.agregarUsuario("u1"); grafo.agregarUsuario("u2"); grafo.conectar("u1", "u2");
        grafo.vaciar();
        assertTrue(grafo.obtenerAmigos("u1").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> grafo.conectar("u1", "u2"));
        grafo.agregarUsuario("u1"); grafo.agregarUsuario("u2"); grafo.conectar("u1", "u2");
        assertTrue(grafo.sonAmigos("u1", "u2"));
    }

    @Test void elVerticeExponeSuIdYSusVecinosSinDuplicados() {
        VerticeAmigos vertice = new VerticeAmigos("u1");
        vertice.agregarAdyacente("u2"); vertice.agregarAdyacente("u2");
        assertTrue(vertice.contieneAdyacente("u2")); assertEquals(1, vertice.getAdyacentes().size());
        vertice.eliminarAdyacente("u2");
        assertFalse(vertice.contieneAdyacente("u2"));
        assertEquals("u1", vertice.getUsuarioId());
        assertThrows(IllegalArgumentException.class, () -> new VerticeAmigos(null));
    }
    @Test void idsConEspaciosSeDesconectanEnAmbosSentidos() {
        grafo.agregarUsuario("u1"); grafo.agregarUsuario("u2");
        grafo.conectar(" u1 ", " u2 ");
        assertTrue(grafo.sonAmigos("u1", " u2 "));
        grafo.desconectar(" u1 ", "u2");
        assertTrue(grafo.obtenerAmigos("u1").isEmpty());
        assertTrue(grafo.obtenerAmigos("u2").isEmpty());
    }
}
