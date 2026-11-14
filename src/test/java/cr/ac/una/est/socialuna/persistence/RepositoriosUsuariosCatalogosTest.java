package cr.ac.una.est.socialuna.persistence;

import com.google.gson.JsonParser;
import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.persistence.RepositorioCarrerasJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioSedesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioUsuariosJSON;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RepositoriosUsuariosCatalogosTest {
    @TempDir Path directorio;

    //Prueba que usuarios, carreras y sedes queden en archivos separados y que los IDs coincidan al reabrir.
    @Test void guardaTresArchivosIndependientesYResuelveIdsAlReabrir() throws Exception {
        var gestor = new GestorJSON(directorio);
        var usuarios = new RepositorioUsuariosJSON(gestor);
        var carreras = new RepositorioCarrerasJSON(gestor);
        var sedes = new RepositorioSedesJSON(gestor);
        assertTrue(usuarios.listarUsuarios().isEmpty());
        assertTrue(carreras.listarCarreras().isEmpty());
        assertTrue(sedes.listarSedes().isEmpty());
        carreras.guardarCarreras(List.of(new Carrera("c1", "Informática", true)));
        assertFalse(Files.exists(directorio.resolve("sedes.json")));
        sedes.guardarSedes(List.of(new SedeCampus("s1", "Alajuela", "SIUA", "Campus", true)));
        String sedesOriginal = leer("sedes.json");
        usuarios.guardar(usuario("u1", "ana", "c1", "s1"));
        usuarios.guardar(usuario("u2", "beto", null, null));
        String usuariosOriginal = leer("usuarios.json");
        var registros = JsonParser.parseString(usuariosOriginal).getAsJsonArray();
        assertEquals(2, registros.size());
        var perfil = registros.get(0).getAsJsonObject();
        assertEquals("c1", perfil.get("carreraId").getAsString());
        assertEquals("s1", perfil.get("sedeId").getAsString());
        assertFalse(usuariosOriginal.contains("Informática"));
        assertFalse(usuariosOriginal.contains("Alajuela"));
        carreras.guardarCarreras(List.of(new Carrera("c1", "Ingeniería en Sistemas", true)));
        assertEquals(usuariosOriginal, leer("usuarios.json"));
        assertEquals(sedesOriginal, leer("sedes.json"));
        String carrerasOriginal = leer("carreras.json");
        Usuario editado = usuarios.buscarUsuarioPorId("u1");
        editado.setSedeId(null);
        usuarios.guardar(editado);
        assertEquals(carrerasOriginal, leer("carreras.json"));
        assertEquals(sedesOriginal, leer("sedes.json"));

        var reabiertos = new RepositorioUsuariosJSON(new GestorJSON(directorio));
        var carrerasReabiertas = new RepositorioCarrerasJSON(new GestorJSON(directorio));
        var sedesReabiertas = new RepositorioSedesJSON(new GestorJSON(directorio));
        Usuario ana = reabiertos.buscarUsuarioPorNombreUsuario("ana");
        assertEquals("Ingeniería en Sistemas", carrerasReabiertas.buscarCarreraPorId(ana.getCarreraId()).getNombre());
        assertEquals("SIUA", sedesReabiertas.buscarSedePorId("s1").getAlias());
        assertNull(ana.getSedeId());
        assertNull(reabiertos.buscarUsuarioPorId("u2").getCarreraId());
        assertEquals(2, reabiertos.listarUsuarios().size());
    }

    //Prueba que un nombre de usuario duplicado falle y conserve la cuenta previa.
    @Test void rechazaDuplicadosYConservaElUsuarioPrevio() {
        var repo = new RepositorioUsuariosJSON(new GestorJSON(directorio));
        repo.guardar(usuario("u1", "ana", null, null));
        assertThrows(IllegalArgumentException.class, () -> repo.guardar(usuario("u2", "ana", null, null)));
        assertNull(repo.buscarUsuarioPorId("u2"));
        assertEquals("u1", repo.buscarUsuarioPorNombre("Persona u1").getId());
    }

    //Prueba que editar y guardar una copia no cambie el perfil original si falla la escritura.
    @Test void editarUnaCopiaNoAlteraElRepositorioSiFallaElGuardado() throws Exception {
        var repo = new RepositorioUsuariosJSON(new GestorJSON(directorio));
        repo.guardar(usuario("u1", "ana", "c1", null));
        var archivo = directorio.resolve("usuarios.json");
        String original = Files.readString(archivo);
        Files.move(archivo, directorio.resolve("respaldo.json"));
        Files.createDirectory(archivo);
        Files.writeString(archivo.resolve("ocupado.txt"), "bloquea el reemplazo");
        Usuario copia = repo.listarUsuarios().getFirst();
        copia.setCarreraId("c2");
        assertThrows(IllegalStateException.class, () -> repo.guardar(copia));
        assertEquals("c1", repo.buscarUsuarioPorId("u1").getCarreraId());
        assertEquals(original, leer("respaldo.json"));
        try (var archivos = Files.list(directorio)) {
            assertFalse(archivos.anyMatch(ruta -> ruta.toString().endsWith(".tmp")));
        }
    }

    //Prueba que una recarga fallida deje intactos los datos en memoria y el archivo.
    @Test void recargaInvalidaConservaElUltimoEstadoYElArchivo() throws Exception {
        var gestor = new GestorJSON(directorio);
        var usuarios = new RepositorioUsuariosJSON(gestor);
        usuarios.guardar(usuario("u1", "ana", null, null));
        String malFormado = "{usuarios: []}";
        Files.writeString(directorio.resolve("usuarios.json"), malFormado);
        assertThrows(IllegalStateException.class, usuarios::cargar);
        assertEquals("u1", usuarios.buscarUsuarioPorId("u1").getId());
        assertEquals(malFormado, leer("usuarios.json"));
        Files.writeString(directorio.resolve("usuarios.json"), "[{}]");
        assertThrows(IllegalStateException.class, usuarios::cargar);
        assertEquals(1, usuarios.listarUsuarios().size());
    }

    //Prueba que un catálogo con IDs repetidos no reemplace el archivo guardado.
    @Test void rechazaCatalogosConIdRepetidoSinReescribirElArchivo() throws Exception {
        var repo = new RepositorioCarrerasJSON(new GestorJSON(directorio));
        var inicial = new Carrera("c1", "Informática", true);
        repo.guardarCarreras(List.of(inicial));
        String original = leer("carreras.json");
        assertThrows(IllegalArgumentException.class, () -> repo.guardarCarreras(List.of(inicial, inicial)));
        assertEquals(original, leer("carreras.json"));
        assertEquals(1, repo.listarCarreras().size());
    }

    //Prueba que un JSON de sedes inválido no bloquee el catálogo de carreras.
    @Test void sedesInvalidasNoImpidenCargarNiGuardarCarreras() throws Exception {
        Files.writeString(directorio.resolve("sedes.json"), "{");
        var gestor = new GestorJSON(directorio);
        assertThrows(IllegalStateException.class, () -> new RepositorioSedesJSON(gestor));
        var carreras = new RepositorioCarrerasJSON(gestor);
        carreras.guardarCarreras(List.of(new Carrera("c1", "Informática", true)));
        assertEquals("Informática", new RepositorioCarrerasJSON(gestor).buscarCarreraPorId("c1").getNombre());
        assertEquals("{", leer("sedes.json"));
    }

    //Prueba que un JSON de carreras inválido no bloquee el catálogo de sedes.
    @Test void carrerasInvalidasNoImpidenCargarNiGuardarSedes() throws Exception {
        Files.writeString(directorio.resolve("carreras.json"), "{");
        var gestor = new GestorJSON(directorio);
        assertThrows(IllegalStateException.class, () -> new RepositorioCarrerasJSON(gestor));
        var sedes = new RepositorioSedesJSON(gestor);
        sedes.guardarSedes(List.of(new SedeCampus("s1", "Alajuela", "SIUA", "Campus", true)));
        assertEquals("SIUA", new RepositorioSedesJSON(gestor).buscarSedePorId("s1").getAlias());
        assertEquals("{", leer("carreras.json"));
    }

    //Recibe el nombre del archivo y devuelve su texto desde la carpeta temporal.
    private String leer(String archivo) throws Exception {
        return Files.readString(directorio.resolve(archivo));
    }

    //Recibe ID, alias, carrera y sede. Devuelve un perfil de prueba con esos datos.
    private Usuario usuario(String id, String alias, String carreraId, String sedeId) {
        return new Usuario(id, "Persona " + id, alias, "a".repeat(64), carreraId, sedeId);
    }
}
