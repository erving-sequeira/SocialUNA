package cr.ac.una.est.socialuna.service;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.persistence.RepositorioCarrerasJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioSedesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioUsuariosJSON;
import cr.ac.una.est.socialuna.persistence.GestorJSON;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServicioUsuariosTest {
    private static final String HASH_ABC = "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad";
    @TempDir Path datos;
    private RepositorioUsuariosJSON usuarios;
    private RepositorioCarrerasJSON carreras;
    private RepositorioSedesJSON sedes;
    private ServicioUsuarios servicio;

    //Crea los catálogos temporales que se usan para registrar y editar perfiles.
    @BeforeEach void prepararCatalogos() {
        var gestor = new GestorJSON(datos);
        usuarios = new RepositorioUsuariosJSON(gestor);
        carreras = new RepositorioCarrerasJSON(gestor);
        sedes = new RepositorioSedesJSON(gestor);
        carreras.guardarCarreras(List.of(new Carrera("c1", "Informática", true), new Carrera("c2", "Arte", true),
                new Carrera("c-inactiva", "Retirada", false)));
        sedes.guardarSedes(List.of(new SedeCampus("s1", "Alajuela", "SIUA", "Campus", true),
                new SedeCampus("s2", "Heredia", "Central", "Campus", true),
                new SedeCampus("s-inactiva", "Retirada", null, "Campus", false)));
        servicio = new ServicioUsuarios(usuarios, sedes, carreras);
    }

    //Prueba que el registro guarde UUID, hash y referencias sin reemplazar otra cuenta.
    @Test void registraConUuidHashYReferenciasSinSobrescribirOtraCuenta() {
        var ana = servicio.registrar("Ana María", " ANA ", "abc", " c1 ", " s1 ");
        var beto = servicio.registrar("Beto", "beto", "abc", "c2", "s2");
        assertDoesNotThrow(() -> UUID.fromString(ana.getId()));
        assertNotEquals(ana.getId(), beto.getId());
        assertEquals("ana", ana.getNombreUsuario());
        assertEquals(HASH_ABC, ana.getPasswordHash());
        var reabiertos = new RepositorioUsuariosJSON(new GestorJSON(datos));
        assertEquals(2, reabiertos.listarUsuarios().size());
        assertEquals("c1", reabiertos.buscarUsuarioPorId(ana.getId()).getCarreraId());
        assertEquals("s1", reabiertos.buscarUsuarioPorId(ana.getId()).getSedeId());
        assertNull(servicio.getUsuarioActual());
    }

    //Prueba que no se pueda registrar un alias repetido ni usar una carrera o sede inválida.
    @Test void registroRechazaAliasRepetidoYCatalogosInvalidos() {
        servicio.registrar("Ana", "Ana", "abc", "c1", "s1");
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("Otra", " ANA ", "abc", "c1", "s1"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("Beto", "beto", "abc", null, "s1"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("Beto", "beto", "abc", "c1", "  "));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("Beto", "beto", "abc", "no-existe", "s1"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("Beto", "beto", "abc", "c1", "s-inactiva"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("Beto", "beto", "abc", "c-inactiva", "s1"));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("Beto", "beto", null, "c1", "s1"));
        assertEquals(1, usuarios.listarUsuarios().size());
        assertEquals(List.of("c1", "c2"), servicio.listarCarreras().stream().map(Carrera::getId).toList());
        assertEquals(List.of("s1", "s2"), servicio.listarSedes().stream().map(SedeCampus::getId).toList());
    }

    //Prueba que el login valide la contraseña y maneje la sesión activa.
    @Test void loginExigeContrasenaCorrectaYControlaLaSesion() {
        var ana = servicio.registrar("Ana", "ANA", " abc ", "c1", "s1");
        assertThrows(IllegalArgumentException.class, () -> servicio.iniciarSesion("ana", "abc"));
        assertNull(servicio.getUsuarioActual());
        assertEquals(ana.getId(), servicio.iniciarSesion(" ANA ", " abc ").getId());
        assertThrows(IllegalStateException.class, () -> servicio.iniciarSesion("ana", " abc "));
        servicio.cerrarSesion();
        assertNull(servicio.getUsuarioActual());
    }

    //Prueba que una cuenta anterior pueda entrar con su alias y conserve el ID.
    @Test void permiteLoginDeUnAliasAnteriorSinCambiarSuId() {
        usuarios.guardar(new Usuario("id-anterior", "Ana", "Ana", HASH_ABC, null, null));
        assertThrows(IllegalArgumentException.class, () -> servicio.registrar("Otra", "ana", "abc", "c1", "s1"));
        assertEquals("id-anterior", servicio.iniciarSesion("ana", "abc").getId());
    }

    //Prueba que la búsqueda use nombre o alias y que el perfil se pueda abrir por ID.
    @Test void buscaPorNombreOAliasYObtienePerfilPorId() {
        var ana = servicio.registrar("Ana María", "estudiante", "abc", "c1", "s1");
        servicio.registrar("Beto", "beto", "abc", "c1", "s1");
        assertEquals(List.of(ana.getId()), servicio.buscarUsuarios(" MARÍA ").stream().map(Usuario::getId).toList());
        assertEquals(List.of(ana.getId()), servicio.buscarUsuarios("ESTUDI").stream().map(Usuario::getId).toList());
        assertTrue(servicio.buscarUsuarios("  ").isEmpty());
        assertEquals("Ana María", servicio.obtenerPerfil(ana.getId()).getNombre());
        assertNull(servicio.obtenerPerfil("id-inexistente"));
    }

    //Prueba que los cambios de carrera y sede se vean en sesión y al reabrir el repositorio.
    @Test void actualizaElPerfilDeLaSesionYLoConservaAlReabrir() {
        var ana = servicio.registrar("Ana", "ana", "abc", "c1", "s1");
        assertThrows(IllegalStateException.class, () -> servicio.actualizarDatosAcademicos("c2", "s2"));
        servicio.iniciarSesion("ana", "abc");
        var actualizado = servicio.actualizarDatosAcademicos(" c2 ", " s2 ");
        assertEquals(ana.getId(), actualizado.getId());
        assertEquals("c2", actualizado.getCarreraId());
        assertEquals("s2", servicio.getUsuarioActual().getSedeId());
        actualizado.setSedeId("s1");
        assertEquals("s2", servicio.getUsuarioActual().getSedeId());
        assertEquals("s2", new RepositorioUsuariosJSON(new GestorJSON(datos)).buscarUsuarioPorId(ana.getId()).getSedeId());
    }

    //Prueba que un fallo de guardado conserve los datos previos en sesión y en el repositorio.
    @Test void falloAlGuardarElPerfilConservaLaSesionYElRepositorio() throws Exception {
        var ana = servicio.registrar("Ana", "ana", "abc", "c1", "s1");
        servicio.iniciarSesion("ana", "abc");
        Path archivo = datos.resolve("usuarios.json");
        Files.move(archivo, datos.resolve("respaldo.json"));
        Files.createDirectory(archivo);
        Files.writeString(archivo.resolve("ocupado.txt"), "bloquea el reemplazo");
        assertThrows(IllegalStateException.class, () -> servicio.actualizarDatosAcademicos("c2", "s2"));
        assertEquals("c1", servicio.getUsuarioActual().getCarreraId());
        assertEquals("s1", usuarios.buscarUsuarioPorId(ana.getId()).getSedeId());
    }
}
