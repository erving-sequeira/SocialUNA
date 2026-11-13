package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Comentario;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.ServicioPublicacion;
import cr.ac.una.est.socialuna.service.ServicioComunidad;
import cr.ac.una.est.socialuna.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class PublicacionesIntegracionTest {
    @TempDir Path datos;

    @Test void feedSoloIncluyeMuroPropioYDeAmigosPorFechaDescendente() {
        var repo = repositorio();
        repo.guardar(new Publicacion("propia", "ana", "Hola", 1, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        repo.guardar(new Publicacion("amigo", "beto", "Hola", 2, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        repo.guardar(new Publicacion("otra-carrera", "beto", "Comunidad", 3, AmbitoPublicacion.CARRERA, TipoPublicacion.PREGUNTA, "c2", null));
        repo.guardar(new Publicacion("mi-sede", "ana", "Comunidad", 4, AmbitoPublicacion.SEDE, TipoPublicacion.GENERAL, null, "s1"));
        repo.guardar(new Publicacion("desconocido", "cata", "Hola", 5, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        var servicio = servicio(repositorio(), List.of(usuario("beto")));
        assertEquals(List.of("amigo", "propia"), servicio.obtenerFeed().stream().map(Publicacion::getId).toList());
    }

    @Test void publicarGuardaMuroGeneralYRecuperaElAutorAlReabrir() {
        var servicio = servicio(repositorio(), List.of());
        assertThrows(IllegalArgumentException.class, () -> servicio.publicar("  "));
        var creada = servicio.publicar(" Hola ");
        var reabierta = repositorio().buscarPorId(creada.getId());
        assertEquals("ana", reabierta.getAutorId());
        assertEquals("Hola", reabierta.getTexto());
        assertEquals(AmbitoPublicacion.MURO, reabierta.getAmbito());
        assertEquals(TipoPublicacion.GENERAL, reabierta.getTipo());
    }

    @Test void rechazaPublicacionesInvalidasSinCambiarDiscoNiMemoria() throws Exception {
        var repo = repositorio();
        repo.guardar(new Publicacion("p1", "ana", "Hola", 1, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        Path archivo = datos.resolve("publicaciones.json");
        for (String contenido : List.of("{\"publicaciones\":null}", "{\"publicaciones\":[{}]}", "{\"publicaciones\":[null]}")) {
            Files.writeString(archivo, contenido);
            assertThrows(IllegalStateException.class, repo::cargar);
            assertEquals(contenido, Files.readString(archivo));
            assertEquals(List.of("p1"), repo.listar().stream().map(Publicacion::getId).toList());
        }
    }

    @Test void cargaLosComentariosValidosDeUnaPublicacion() {
        var repo = repositorio();
        var publicacion = new Publicacion("p1", "ana", "Consulta", 1, AmbitoPublicacion.CARRERA, TipoPublicacion.PREGUNTA, "c1", null);
        publicacion.agregarComentario(new Comentario("r1", "beto", "Respuesta", 2));
        repo.guardar(publicacion);
        var comentario = repositorio().buscarPorId("p1").getComentarios().getFirst();
        assertEquals("beto", comentario.getAutorId());
        assertEquals("Respuesta", comentario.getTexto());
    }

    @Test void publicarEnCarreraConservaSedeParaFiltrarInclusoAlReabrir() {
        var repo = repositorio();
        var ana = comunidad(repo, usuario("ana"));
        var beto = comunidad(repo, new Usuario("beto", "Beto", "beto", "a".repeat(64), "c1", "s2"));
        var deS1 = ana.publicarEnCarrera("Consulta", TipoPublicacion.PREGUNTA);
        var deS2 = beto.publicarEnCarrera("Otra consulta", TipoPublicacion.GENERAL);
        ana.publicarEnSede("Comunidad de sede", TipoPublicacion.GENERAL);
        var reabierta = comunidad(repositorio(), usuario("ana"));
        assertEquals("s1", deS1.getSedeId());
        assertEquals(List.of(deS1.getId()), reabierta.obtenerPorCarrera(" s1 ").stream().map(Publicacion::getId).toList());
        assertEquals(List.of(deS2.getId()), reabierta.obtenerPorCarrera("s2").stream().map(Publicacion::getId).toList());
        assertEquals(2, reabierta.obtenerPorCarrera(null).size());
        assertEquals(1, reabierta.obtenerPorSede().size());
    }

    @Test void comentarioVisiblePersisteYElMuroYOtrasCarrerasQuedanFuera() {
        var repo = repositorio();
        var ana = comunidad(repo, usuario("ana"));
        var otraCarrera = comunidad(repo, new Usuario("beto", "Beto", "beto", "a".repeat(64), "c2", "s1"));
        var restringida = otraCarrera.publicarEnCarrera("Carrera c2", TipoPublicacion.GENERAL);
        var sedeCompartida = otraCarrera.publicarEnSede("Sede s1", TipoPublicacion.GENERAL);
        var muro = servicio(repo, List.of()).publicar("Muro");
        assertThrows(IllegalArgumentException.class, () -> ana.comentar(restringida.getId(), "Hola"));
        assertThrows(IllegalArgumentException.class, () -> ana.obtenerComentarios(restringida.getId()));
        assertThrows(IllegalArgumentException.class, () -> ana.comentar(muro.getId(), "Hola"));
        var comentario = ana.comentar(" " + sedeCompartida.getId() + " ", " Respuesta ");
        var guardado = comunidad(repositorio(), usuario("ana")).obtenerComentarios(sedeCompartida.getId()).getFirst();
        assertEquals(comentario.getId(), guardado.getId());
        assertEquals("ana", guardado.getAutorId());
        assertEquals("Respuesta", guardado.getTexto());
    }

    @Test void comentarioQueNoSeGuardaNoApareceEnMemoria() throws Exception {
        var repo = repositorio();
        var ana = comunidad(repo, usuario("ana"));
        var publicacion = ana.publicarEnCarrera("Consulta", TipoPublicacion.PREGUNTA);
        ana.comentar(publicacion.getId(), "Guardado");
        Path archivo = datos.resolve("publicaciones.json");
        Files.move(archivo, datos.resolve("respaldo.json"));
        Files.createDirectory(archivo);
        Files.writeString(archivo.resolve("ocupado.txt"), "bloquea el reemplazo");
        assertThrows(IllegalStateException.class, () -> ana.comentar(publicacion.getId(), "No guardado"));
        assertEquals(List.of("Guardado"), ana.obtenerComentarios(publicacion.getId()).stream().map(Comentario::getTexto).toList());
    }

    @Test void publicacionesConIgualFechaSeOrdenanPorIdEnMuroYComunidad() {
        var repo = repositorio();
        for (String id : List.of("b", "a")) {
            repo.guardar(new Publicacion("muro-" + id, "ana", "Hola", 1, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
            repo.guardar(new Publicacion("sede-" + id, "ana", "Hola", 1, AmbitoPublicacion.SEDE, TipoPublicacion.GENERAL, null, "s1"));
        }
        assertEquals(List.of("muro-a", "muro-b"), servicio(repo, List.of()).obtenerFeed().stream().map(Publicacion::getId).toList());
        assertEquals(List.of("sede-a", "sede-b"), comunidad(repo, usuario("ana")).obtenerPorSede().stream().map(Publicacion::getId).toList());
    }

    private ServicioComunidad comunidad(RepositorioPublicacionesJSON repo, Usuario usuario) {
        return new ServicioComunidad(doble(IServicioUsuarios.class, "getUsuarioActual", usuario), repo);
    }

    private RepositorioPublicacionesJSON repositorio() {
        var repo = new RepositorioPublicacionesJSON(new GestorJSON(datos));
        repo.cargar();
        return repo;
    }

    private ServicioPublicacion servicio(RepositorioPublicacionesJSON repo, List<Usuario> amigos) {
        return new ServicioPublicacion(doble(IServicioUsuarios.class, "getUsuarioActual", usuario("ana")),
                doble(IServicioAmistad.class, "listarMisAmigos", new ArrayList<>(amigos)), repo);
    }

    private static Usuario usuario(String id) {
        return new Usuario(id, id, id, "a".repeat(64), "c1", "s1");
    }

    private static <T> T doble(Class<T> tipo, String operacion, Object respuesta) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, (proxy, metodo, args) -> {
            if (!operacion.equals(metodo.getName())) throw new AssertionError("Operación inesperada: " + metodo.getName());
            return respuesta;
        }));
    }
}
