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

    //Prueba que el feed solo muestre posts del muro propio y de amigos, con los más nuevos primero.
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

    //Prueba que el post del muro conserve al autor al reabrir el repositorio.
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

    //Prueba que un post inválido deje igual el archivo y la lista en memoria.
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

    //Prueba que al reabrir los posts se recuperen sus comentarios válidos.
    @Test void cargaLosComentariosValidosDeUnaPublicacion() {
        var repo = repositorio();
        var publicacion = new Publicacion("p1", "ana", "Consulta", 1, AmbitoPublicacion.CARRERA, TipoPublicacion.PREGUNTA, "c1", null);
        publicacion.agregarComentario(new Comentario("r1", "beto", "Respuesta", 2));
        repo.guardar(publicacion);
        var comentario = repositorio().buscarPorId("p1").getComentarios().getFirst();
        assertEquals("beto", comentario.getAutorId());
        assertEquals("Respuesta", comentario.getTexto());
    }

    //Prueba que los posts de carrera se puedan filtrar por sede después de reabrir el repositorio.
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

    //Prueba que se guarde un comentario permitido y que las publicaciones ajenas queden fuera.
    @Test void comentarioVisiblePersisteYLasPublicacionesAjenasQuedanFuera() {
        var repo = repositorio();
        var ana = comunidad(repo, usuario("ana"));
        var otraCarrera = comunidad(repo, new Usuario("beto", "Beto", "beto", "a".repeat(64), "c2", "s1"));
        var restringida = otraCarrera.publicarEnCarrera("Carrera c2", TipoPublicacion.GENERAL);
        var sedeCompartida = otraCarrera.publicarEnSede("Sede s1", TipoPublicacion.GENERAL);
        var muroServicio = servicio(repo, List.of());
        var muro = muroServicio.publicar("Muro");
        assertThrows(IllegalArgumentException.class, () -> ana.comentar(restringida.getId(), "Hola"));
        assertThrows(IllegalArgumentException.class, () -> ana.obtenerComentarios(restringida.getId()));
        assertEquals("Hola muro", muroServicio.comentar(muro.getId(), "Hola muro").getTexto());
        var comentario = ana.comentar(" " + sedeCompartida.getId() + " ", " Respuesta ");
        var guardado = comunidad(repositorio(), usuario("ana")).obtenerComentarios(sedeCompartida.getId()).getFirst();
        assertEquals(comentario.getId(), guardado.getId());
        assertEquals("ana", guardado.getAutorId());
        assertEquals("Respuesta", guardado.getTexto());
    }

    //Prueba que el autor elimine su post con todos sus comentarios y que el cambio sobreviva al reabrir.
    @Test void eliminarPublicacionPropiaBorraSusComentariosYRespuestas() {
        var repo = repositorio();
        var servicio = servicio(repo, List.of());
        var publicacion = servicio.publicar("Hola");
        var comentario = servicio.comentar(publicacion.getId(), "Comentario");
        servicio.responder(publicacion.getId(), comentario.getId(), "Respuesta");

        servicio.eliminarPublicacion(publicacion.getId());

        assertNull(repo.buscarPorId(publicacion.getId()));
        assertNull(repositorio().buscarPorId(publicacion.getId()));
    }

    //Prueba que nadie más pueda borrar un post y que el muro no borre publicaciones de comunidad.
    @Test void eliminarPublicacionAjenaOFueraDelMuroFallaYNoBorra() {
        var repo = repositorio();
        repo.guardar(new Publicacion("ajena", "beto", "Hola", 1, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        repo.guardar(new Publicacion("carrera", "ana", "Hola", 2, AmbitoPublicacion.CARRERA, TipoPublicacion.GENERAL, "c1", "s1"));
        var ana = servicio(repo, List.of());

        assertThrows(IllegalStateException.class, () -> ana.eliminarPublicacion("ajena"));
        assertThrows(IllegalArgumentException.class, () -> ana.eliminarPublicacion("carrera"));
        assertThrows(IllegalArgumentException.class, () -> ana.eliminarPublicacion("no-existe"));
        assertNotNull(repo.buscarPorId("ajena"));
        assertNotNull(repo.buscarPorId("carrera"));
    }

    //Prueba que responder guarde el padre y que una respuesta a otra respuesta quede en la raíz del hilo.
    @Test void responderGuardaElPadreYNormalizaLasRespuestasAnidadasALaRaiz() {
        var repo = repositorio();
        var servicio = servicio(repo, List.of());
        var publicacion = servicio.publicar("Hilo");
        var raiz = servicio.comentar(publicacion.getId(), "Raiz");
        var respuesta = servicio.responder(publicacion.getId(), raiz.getId(), "Respuesta");
        var anidada = servicio.responder(publicacion.getId(), respuesta.getId(), "Respuesta a respuesta");

        assertEquals(raiz.getId(), respuesta.getRespuestaAId());
        assertEquals(raiz.getId(), anidada.getRespuestaAId());
        assertThrows(IllegalArgumentException.class,
                () -> servicio.responder(publicacion.getId(), "no-existe", "Hola"));

        var guardados = repositorio().buscarPorId(publicacion.getId()).getComentarios();
        assertEquals(3, guardados.size());
        assertEquals(raiz.getId(), guardados.get(1).getRespuestaAId());
        assertEquals(raiz.getId(), guardados.get(2).getRespuestaAId());
    }

    //Prueba que el muro solo permita comentar y ver comentarios de posts propios o de amigos.
    @Test void comentariosDelMuroSoloParaPublicacionesPropiasODAmigos() {
        var repo = repositorio();
        var ana = servicio(repo, List.of());
        var propia = ana.publicar("Propia");
        assertEquals("Hola", ana.comentar(propia.getId(), "Hola").getTexto());

        repo.guardar(new Publicacion("amigo", "beto", "Amigo", 2, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        var conAmigo = servicio(repo, List.of(usuario("beto")));
        assertEquals("Hola", conAmigo.comentar("amigo", "Hola").getTexto());

        repo.guardar(new Publicacion("desconocido", "cata", "Ajeno", 3, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        assertThrows(IllegalArgumentException.class, () -> conAmigo.obtenerComentarios("desconocido"));
        assertThrows(IllegalArgumentException.class, () -> conAmigo.comentar("desconocido", "Hola"));
    }

    //Prueba que en comunidad solo el autor elimine su publicación y que el cambio se guarde.
    @Test void eliminarPublicacionDeComunidadSoloLaBorraElAutor() {
        var repo = repositorio();
        var ana = comunidad(repo, usuario("ana"));
        var propia = ana.publicarEnSede("Propia", TipoPublicacion.GENERAL);
        var beto = comunidad(repo, new Usuario("beto", "Beto", "beto", "a".repeat(64), "c1", "s1"));

        assertThrows(IllegalStateException.class, () -> beto.eliminarPublicacion(propia.getId()));
        ana.eliminarPublicacion(propia.getId());

        assertNull(repositorio().buscarPorId(propia.getId()));
    }

    //Prueba que una publicación no quede fuera de memoria si falló la escritura al eliminarla.
    @Test void publicacionQueNoSeEliminaSigueEnMemoria() throws Exception {
        var repo = repositorio();
        var servicio = servicio(repo, List.of());
        var publicacion = servicio.publicar("Hola");
        Path archivo = datos.resolve("publicaciones.json");
        Files.move(archivo, datos.resolve("respaldo.json"));
        Files.createDirectory(archivo);
        Files.writeString(archivo.resolve("ocupado.txt"), "bloquea el reemplazo");

        assertThrows(IllegalStateException.class, () -> servicio.eliminarPublicacion(publicacion.getId()));
        assertNotNull(repo.buscarPorId(publicacion.getId()));
    }

    //Prueba que un comentario no quede en memoria si falló la escritura.
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

    //Prueba que muro y comunidad usen el ID para ordenar posts con la misma fecha.
    @Test void publicacionesConIgualFechaSeOrdenanPorIdEnMuroYComunidad() {
        var repo = repositorio();
        for (String id : List.of("b", "a")) {
            repo.guardar(new Publicacion("muro-" + id, "ana", "Hola", 1, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
            repo.guardar(new Publicacion("sede-" + id, "ana", "Hola", 1, AmbitoPublicacion.SEDE, TipoPublicacion.GENERAL, null, "s1"));
        }
        assertEquals(List.of("muro-a", "muro-b"), servicio(repo, List.of()).obtenerFeed().stream().map(Publicacion::getId).toList());
        assertEquals(List.of("sede-a", "sede-b"), comunidad(repo, usuario("ana")).obtenerPorSede().stream().map(Publicacion::getId).toList());
    }

    //Prueba que borrar un comentario raíz elimine también todas sus respuestas.
    @Test void eliminarComentarioPropioBorraTodoElHilo() {
        var repo = repositorio();
        var servicio = servicio(repo, List.of());
        var publicacion = servicio.publicar("Hilo");
        var raiz = servicio.comentar(publicacion.getId(), "Raiz");
        servicio.responder(publicacion.getId(), raiz.getId(), "Respuesta");

        servicio.eliminarComentario(publicacion.getId(), raiz.getId());

        assertTrue(repositorio().buscarPorId(publicacion.getId()).getComentarios().isEmpty());
        assertThrows(IllegalArgumentException.class,
                () -> servicio.eliminarComentario(publicacion.getId(), raiz.getId()));
    }

    //Prueba que el autor de un comentario ajeno no pueda borrarlo.
    @Test void eliminarComentarioAjenoFallaYNoBorra() {
        var repo = repositorio();
        var ana = comunidad(repo, usuario("ana"));
        var publicacion = ana.publicarEnSede("Post", TipoPublicacion.GENERAL);
        var comentario = ana.comentar(publicacion.getId(), "Mío");
        var beto = comunidad(repo, new Usuario("beto", "Beto", "beto", "a".repeat(64), "c1", "s1"));

        assertThrows(IllegalStateException.class,
                () -> beto.eliminarComentario(publicacion.getId(), comentario.getId()));
        assertEquals(1, ana.obtenerComentarios(publicacion.getId()).size());
    }

    //Prueba que borrar solo una respuesta deje intacto el comentario raíz.
    @Test void eliminarRespuestaDejaLaRaiz() {
        var repo = repositorio();
        var servicio = servicio(repo, List.of());
        var publicacion = servicio.publicar("Hilo");
        var raiz = servicio.comentar(publicacion.getId(), "Raiz");
        var respuesta = servicio.responder(publicacion.getId(), raiz.getId(), "Respuesta");

        servicio.eliminarComentario(publicacion.getId(), respuesta.getId());

        var comentarios = servicio.obtenerComentarios(publicacion.getId());
        assertEquals(1, comentarios.size());
        assertEquals(raiz.getId(), comentarios.getFirst().getId());
    }

    //Prueba que el me gusta se alterne, persista y no se pierda al comentar.
    @Test void reaccionSeAlternaPersisteYNoSePierdeAlComentar() {
        var repo = repositorio();
        var servicio = servicio(repo, List.of());
        var publicacion = servicio.publicar("Me gusta");

        assertTrue(servicio.alternarReaccion(publicacion.getId()).leGustaA("ana"));
        servicio.comentar(publicacion.getId(), "Comentario");

        var reabierta = repositorio().buscarPorId(publicacion.getId());
        assertTrue(reabierta.leGustaA("ana"));
        assertEquals(1, reabierta.getTotalReacciones());

        assertFalse(servicio.alternarReaccion(publicacion.getId()).leGustaA("ana"));
        assertEquals(0, repositorio().buscarPorId(publicacion.getId()).getTotalReacciones());
    }

    //Prueba que dos usuarios sumen reacciones y que el muro no reaccione a posts ajenos.
    @Test void reaccionDeVariosUsuariosYVisibilidadDelMuro() {
        var repo = repositorio();
        repo.guardar(new Publicacion("ajena", "cata", "Hola", 1, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        var ana = servicio(repo, List.of());

        assertThrows(IllegalArgumentException.class, () -> ana.alternarReaccion("ajena"));

        var propia = ana.publicar("Propia");
        ana.alternarReaccion(propia.getId());
        var beto = servicio(repo, List.of(usuario("ana")), new Usuario("beto", "Beto", "beto", "a".repeat(64), "c1", "s1"));
        var actualizada = beto.alternarReaccion(propia.getId());

        assertEquals(2, actualizada.getTotalReacciones());
        assertTrue(actualizada.leGustaA("ana"));
        assertTrue(actualizada.leGustaA("beto"));
    }

    //Prueba que una reacción de comunidad también se guarde en el archivo.
    @Test void reaccionDeComunidadSeGuarda() {
        var repo = repositorio();
        var ana = comunidad(repo, usuario("ana"));
        var publicacion = ana.publicarEnSede("Propia", TipoPublicacion.GENERAL);

        assertTrue(ana.alternarReaccion(publicacion.getId()).leGustaA("ana"));
        assertTrue(repositorio().buscarPorId(publicacion.getId()).leGustaA("ana"));
    }

    //Prueba que el perfil solo muestre publicaciones de muro propias o de amigos.
    @Test void obtenerPublicacionesDeSoloMuroVisible() {
        var repo = repositorio();
        repo.guardar(new Publicacion("propia", "ana", "A", 1, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        repo.guardar(new Publicacion("amigo", "beto", "B", 2, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        repo.guardar(new Publicacion("ajena", "cata", "C", 3, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
        repo.guardar(new Publicacion("carrera", "beto", "D", 4, AmbitoPublicacion.CARRERA, TipoPublicacion.GENERAL, "c1", "s1"));
        var servicio = servicio(repo, List.of(usuario("beto")));

        assertEquals(List.of("propia"), servicio.obtenerPublicacionesDe("ana").stream().map(Publicacion::getId).toList());
        assertEquals(List.of("amigo"), servicio.obtenerPublicacionesDe("beto").stream().map(Publicacion::getId).toList());
        assertTrue(servicio.obtenerPublicacionesDe("cata").isEmpty());
    }

    //Recibe un repositorio y un usuario. Devuelve un servicio de comunidad con ese usuario en sesión.
    private ServicioComunidad comunidad(RepositorioPublicacionesJSON repo, Usuario usuario) {
        return new ServicioComunidad(doble(IServicioUsuarios.class, "getUsuarioActual", usuario), repo);
    }

    //Abre y devuelve el repositorio de publicaciones en la carpeta temporal de la prueba.
    private RepositorioPublicacionesJSON repositorio() {
        var repo = new RepositorioPublicacionesJSON(new GestorJSON(datos));
        repo.cargar();
        return repo;
    }

    //Recibe el repositorio y la lista de amigos. Devuelve el servicio del muro con Ana en sesión.
    private ServicioPublicacion servicio(RepositorioPublicacionesJSON repo, List<Usuario> amigos) {
        return servicio(repo, amigos, usuario("ana"));
    }

    //Recibe el repositorio, los amigos y el usuario en sesión. Devuelve el servicio del muro.
    private ServicioPublicacion servicio(RepositorioPublicacionesJSON repo, List<Usuario> amigos, Usuario actual) {
        return new ServicioPublicacion(doble(IServicioUsuarios.class, "getUsuarioActual", actual),
                doble(IServicioAmistad.class, "listarMisAmigos", new ArrayList<>(amigos)), repo);
    }

    //Recibe un ID y devuelve un usuario de prueba con carrera y sede fijas.
    private static Usuario usuario(String id) {
        return new Usuario(id, id, id, "a".repeat(64), "c1", "s1");
    }

    //Recibe la interfaz, el nombre del método y una respuesta. Devuelve un sustituto que responde a ese método.
    private static <T> T doble(Class<T> tipo, String operacion, Object respuesta) {
        return tipo.cast(Proxy.newProxyInstance(tipo.getClassLoader(), new Class<?>[]{tipo}, (proxy, metodo, args) -> {
            if (!operacion.equals(metodo.getName())) throw new AssertionError("Operación inesperada: " + metodo.getName());
            return respuesta;
        }));
    }
}
