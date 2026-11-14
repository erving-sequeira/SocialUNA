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

    //Devuelve un repositorio de amistades que usa la carpeta temporal de la prueba.
    private RepositorioAmistadesJSON nuevoRepositorio() {
        RepositorioAmistadesJSON repositorio = new RepositorioAmistadesJSON(new GestorJSON(directorio));
        repositorio.cargar();
        return repositorio;
    }

    //Recibe el ID y los dos usuarios. Devuelve una amistad de prueba.
    private RelacionAmistad relacion(String id, String a, String b) {
        return new RelacionAmistad(id, a, b, 1730000000000L);
    }

    //Prueba que el repositorio dé error si se usa antes de cargarlo.
    @Test void exigeCargarAntesDeUsarse() {
        RepositorioAmistadesJSON repositorio = new RepositorioAmistadesJSON(new GestorJSON(directorio));
        assertThrows(IllegalStateException.class, repositorio::listar);
        assertThrows(IllegalStateException.class, () -> repositorio.guardar(relacion("r1", "u1", "u2")));
        assertThrows(IllegalArgumentException.class, () -> new RepositorioAmistadesJSON(null));
    }

    //Prueba que sin archivo se cargue una lista de amistades vacía.
    @Test void archivoAusenteSeTrataComoDocumentoVacio() {
        assertTrue(nuevoRepositorio().listar().isEmpty());
        assertNull(nuevoRepositorio().buscarEntre("u1", "u2"));
    }

    //Prueba que una amistad guardada se pueda recuperar después de reabrir.
    @Test void guardaRecuperaYConservaAlReabrir() {
        RepositorioAmistadesJSON repositorio = nuevoRepositorio();
        repositorio.guardar(relacion("r1", "u1", "u2"));
        repositorio.guardar(relacion("r2", "u2", "u3"));

        RepositorioAmistadesJSON reabierto = nuevoRepositorio();
        assertEquals(2, reabierto.listar().size());
        assertEquals("r1", reabierto.buscarEntre("u2", "u1").getId());
        assertNull(reabierto.buscarEntre("u1", "u3"));
    }

    //Prueba que no se pueda repetir un ID de amistad ni agregar el mismo par en orden contrario.
    @Test void rechazaRelacionRepetidaPorIdOPorParSinOrden() {
        RepositorioAmistadesJSON repositorio = nuevoRepositorio();
        repositorio.guardar(relacion("r1", "u1", "u2"));
        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(relacion("r1", "u3", "u4")));
        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(relacion("r2", "u2", "u1")));
        assertThrows(IllegalArgumentException.class, () -> repositorio.guardar(null));
        assertEquals(1, nuevoRepositorio().listar().size());
    }

    //Prueba que la amistad siga borrada al leer el archivo de nuevo.
    @Test void eliminarRetiraLaRelacionDelArchivo() {
        RepositorioAmistadesJSON repositorio = nuevoRepositorio();
        repositorio.guardar(relacion("r1", "u1", "u2"));
        repositorio.eliminar("r1");
        assertTrue(repositorio.listar().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> repositorio.eliminar("no-existe"));
        assertTrue(nuevoRepositorio().listar().isEmpty());
    }

    //Prueba que un JSON inválido dé error y se quede tal como estaba.
    @Test void archivoInvalidoSeInformaSinBorrarSuContenido() throws Exception {
        Path archivo = directorio.resolve("amistades.json");
        String contenido = "{esto no es json";
        Files.writeString(archivo, contenido);

        RepositorioAmistadesJSON repositorio = new RepositorioAmistadesJSON(new GestorJSON(directorio));
        assertThrows(IllegalStateException.class, repositorio::cargar);
        assertEquals(contenido, Files.readString(archivo));
    }

    //Prueba que un fallo de escritura dé error y no agregue la amistad.
    @Test void falloDeEscrituraNoSePresentaComoGuardadoExitoso() throws Exception {
        Path bloqueado = directorio.resolve("bloqueado");
        Files.writeString(bloqueado, "no soy un directorio");
        RepositorioAmistadesJSON repositorio = new RepositorioAmistadesJSON(new GestorJSON(bloqueado));
        repositorio.cargar();

        assertThrows(IllegalStateException.class, () -> repositorio.guardar(relacion("r1", "u1", "u2")));
        assertTrue(repositorio.listar().isEmpty());
    }

    //Prueba que cambiar la lista devuelta no afecte las amistades del repositorio.
    @Test void listarDevuelveUnaCopiaQueNoAlteraElDocumento() {
        RepositorioAmistadesJSON repositorio = nuevoRepositorio();
        repositorio.guardar(relacion("r1", "u1", "u2"));
        ArrayList<RelacionAmistad> listadas = repositorio.listar();
        assertEquals("u1", listadas.get(0).getUsuarioAId());
        listadas.clear();
        assertEquals(1, nuevoRepositorio().listar().size());
    }

    //Prueba que se lean los dos formatos de JSON y que la búsqueda requiera los dos IDs.
    @Test void cargaAmbosFormatosYBuscarEntreExigeDosUsuarios() throws Exception {
        Path archivo = directorio.resolve("amistades.json");
        String arreglo = "[{\"id\":\"r1\",\"usuarioAId\":\"u1\",\"usuarioBId\":\"u2\",\"creadoEn\":1}]";
        for (String contenido : java.util.List.of(arreglo, "{\"relaciones\":" + arreglo + "}")) {
            Files.writeString(archivo, contenido);
            var repo = nuevoRepositorio();
            assertEquals("r1", repo.buscarEntre(" u2 ", "u1").getId());
            assertNull(repo.buscarEntre("u1", " u1 "));
            assertEquals(contenido, Files.readString(archivo));
        }
    }
}
