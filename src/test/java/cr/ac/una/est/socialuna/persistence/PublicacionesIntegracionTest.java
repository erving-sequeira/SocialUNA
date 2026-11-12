package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.logic.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.logic.model.entity.Comentario;
import cr.ac.una.est.socialuna.logic.model.entity.Publicacion;
import cr.ac.una.est.socialuna.logic.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.logic.model.entity.Usuario;
import cr.ac.una.est.socialuna.logic.service.ServicioPublicacion;
import cr.ac.una.est.socialuna.logic.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.logic.service.contract.IServicioUsuarios;
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

    private RepositorioPublicacionesJSON repositorio() {
        var repo = new RepositorioPublicacionesJSON(new GestorJSON(datos));
        repo.cargar();
        return repo;
    }

    private ServicioPublicacion servicio(RepositorioPublicacionesJSON repo, List<Usuario> amigos) {
        return new ServicioPublicacion(doble(IServicioUsuarios.class, "getUsuarioActual", usuario("ana")),
                doble(IServicioAmistad.class, "listarAmigos", new ArrayList<>(amigos)), repo);
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
