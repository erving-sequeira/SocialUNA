package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.model.entity.RelacionAmistad;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RepositorioAmistadesJSONTest {
    @TempDir Path directorio;

    private RepositorioAmistadesJSON nuevoRepositorio() {
        RepositorioAmistadesJSON repositorio = new RepositorioAmistadesJSON(new GestorJSON(directorio));
        repositorio.cargar();
        return repositorio;
    }

    private RelacionAmistad relacion(String id, String a, String b) {
        return new RelacionAmistad(id, a, b, 1730000000000L);
    }

    @Test void exigeCargarAntesDeUsarse() {
        RepositorioAmistadesJSON repositorio = new RepositorioAmistadesJSON(new GestorJSON(directorio));
        assertThrows(IllegalStateException.class, repositorio::listar);
        assertThrows(IllegalStateException.class, () -> repositorio.guardar(relacion("r1", "u1", "u2")));
        assertThrows(IllegalArgumentException.class, () -> new RepositorioAmistadesJSON(null));
    }

    @Test void archivoAusenteSeTrataComoDocumentoVacio() {
        assertTrue(nuevoRepositorio().listar().isEmpty());
        assertNull(nuevoRepositorio().buscarEntre("u1", "u2"));
    }

    @Test void guardaRecuperaYConservaAlReabrir() {
        RepositorioAmistadesJSON repositorio = nuevoRepositorio();
        repositorio.guardar(relacion("r1", "u1", "u2"));
        repositorio.guardar(relacion("r2", "u2", "u3"));

        RepositorioAmistadesJSON reabierto = nuevoRepositorio();
        assertEquals(2, reabierto.listar().size());
        assertEquals("r1", reabierto.buscarEntre("u2", "u1").getId());
        assertNull(reabierto.buscarEntre("u1", "u3"));
    }

    @Test void rechazaRelacionRepetidaPorIdOPorParSinOrden() {
        RepositorioAmistadesJSON repositorio = nuevoRepositorio();
        repositorio.guardar(relacion("r1", "u1", "u2"));
        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(relacion("r1", "u3", "u4")));
        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(relacion("r2", "u2", "u1")));
        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(null));
        assertEquals(1, nuevoRepositorio().listar().size());
    }

    @Test void eliminarRetiraLaRelacionDelArchivo() {
        RepositorioAmistadesJSON repositorio = nuevoRepositorio();
        repositorio.guardar(relacion("r1", "u1", "u2"));
        repositorio.eliminar("r1");
        assertTrue(repositorio.listar().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> repositorio.eliminar("no-existe"));
        assertTrue(nuevoRepositorio().listar().isEmpty());
    }

    @Test void archivoInvalidoSeInformaSinBorrarSuContenido() throws Exception {
        Path archivo = directorio.resolve("amistades.json");
        String contenido = "{esto no es json";
        Files.writeString(archivo, contenido);

        RepositorioAmistadesJSON repositorio = new RepositorioAmistadesJSON(new GestorJSON(directorio));
        assertThrows(IllegalStateException.class, repositorio::cargar);
        assertEquals(contenido, Files.readString(archivo));
    }

    @Test void falloDeEscrituraNoSePresentaComoGuardadoExitoso() throws Exception {
        Path bloqueado = directorio.resolve("bloqueado");
        Files.writeString(bloqueado, "no soy un directorio");
        RepositorioAmistadesJSON repositorio = new RepositorioAmistadesJSON(new GestorJSON(bloqueado));
        repositorio.cargar();

        assertThrows(IllegalStateException.class, () -> repositorio.guardar(relacion("r1", "u1", "u2")));
        assertTrue(repositorio.listar().isEmpty());
    }

    @Test void listarDevuelveUnaCopiaQueNoAlteraElDocumento() {
        RepositorioAmistadesJSON repositorio = nuevoRepositorio();
        repositorio.guardar(relacion("r1", "u1", "u2"));
        ArrayList<RelacionAmistad> listadas = repositorio.listar();
        assertEquals("u1", listadas.get(0).getUsuarioAId());
        listadas.clear();
        assertEquals(1, nuevoRepositorio().listar().size());
    }
}
