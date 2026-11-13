package cr.ac.una.est.socialuna.service;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.RelacionAmistad;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.model.graph.GrafoAmigos;
import cr.ac.una.est.socialuna.persistence.RepositorioAmistadesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioCarrerasJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioSedesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioUsuariosJSON;
import cr.ac.una.est.socialuna.persistence.GestorJSON;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

class ServicioAmistadTest {
    @TempDir Path directorio;
    private RepositorioUsuariosJSON perfiles;
    private RepositorioAmistadesJSON relaciones;
    private ServicioUsuarios usuarios;
    private ServicioAmistad amistad;
    private Usuario ana, beto, cata;

    @BeforeEach void preparar() {
        var gestor = new GestorJSON(directorio);
        perfiles = new RepositorioUsuariosJSON(gestor);
        var carreras = new RepositorioCarrerasJSON(gestor);
        var sedes = new RepositorioSedesJSON(gestor);
        carreras.guardarCarreras(List.of(new Carrera("c1", "Informática", true)));
        sedes.guardarSedes(List.of(new SedeCampus("s1", "Alajuela", "SIUA", "Campus", true)));
        usuarios = new ServicioUsuarios(perfiles, sedes, carreras);
        ana = usuarios.registrar("Ana", "ana", "abc", "c1", "s1");
        beto = usuarios.registrar("Beto", "beto", "abc", "c1", "s1");
        cata = usuarios.registrar("Cata", "cata", "abc", "c1", "s1");
        relaciones = new RepositorioAmistadesJSON(gestor);
        relaciones.cargar();
        amistad = new ServicioAmistad(usuarios, perfiles, relaciones, new GrafoAmigos());
    }

    @Test void distingueMiListaDeLaDelPerfilConsultadoYCalculaAmbosTotales() {
        usuarios.iniciarSesion("beto", "abc");
        amistad.agregarAmigo(cata.getId());
        usuarios.cerrarSesion();
        usuarios.iniciarSesion("ana", "abc");
        assertTrue(amistad.listarMisAmigos().isEmpty());
        assertEquals(0, amistad.contarAmigosDe(ana.getId()));
        amistad.agregarAmigo(beto.getId());
        assertEquals(List.of(beto.getId()), ids(amistad.listarMisAmigos()));
        assertEquals(List.of(cata.getId(), ana.getId()), ids(amistad.listarAmigosDe(beto.getId())));
        assertEquals(1, amistad.contarAmigosDe(ana.getId()));
        assertEquals(2, amistad.contarAmigosDe(beto.getId()));
        assertTrue(amistad.sonAmigos(beto.getId(), cata.getId()));
        assertFalse(amistad.sonAmigos(ana.getId(), cata.getId()));
        amistad.eliminarAmigo(beto.getId());
        assertEquals(0, amistad.contarAmigosDe(ana.getId()));
        assertEquals(1, amistad.contarAmigosDe(beto.getId()));
        assertFalse(amistad.sonAmigos(beto.getId(), ana.getId()));
    }

    @Test void alReabrirReconstruyeLasAmistadesYOmiteReferenciasAUsuariosAusentes() {
        usuarios.iniciarSesion("ana", "abc");
        amistad.agregarAmigo(beto.getId());
        var guardada = relaciones.buscarEntre(ana.getId(), beto.getId());
        assertDoesNotThrow(() -> UUID.fromString(guardada.getId()));
        assertTrue(guardada.getCreadoEn() > 0);
        relaciones.guardar(new RelacionAmistad("vieja", ana.getId(), "usuario-ausente", 0));
        var reabiertas = new RepositorioAmistadesJSON(new GestorJSON(directorio));
        reabiertas.cargar();
        var servicio = new ServicioAmistad(usuarios, perfiles, reabiertas, new GrafoAmigos());
        assertEquals(List.of(beto.getId()), ids(servicio.listarMisAmigos()));
        assertEquals(1, servicio.contarAmigosDe(ana.getId()));
        assertEquals(1, servicio.contarAmigosDe(beto.getId()));
        assertEquals(2, reabiertas.listar().size());
    }

    @Test void exigeSesionYRechazaAutoamistadDuplicadosYPerfilesInexistentes() {
        assertThrows(IllegalStateException.class, amistad::listarMisAmigos);
        assertThrows(IllegalStateException.class, () -> amistad.contarAmigosDe(ana.getId()));
        usuarios.iniciarSesion("ana", "abc");
        assertThrows(IllegalArgumentException.class, () -> amistad.listarAmigosDe("desconocido"));
        assertThrows(IllegalArgumentException.class, () -> amistad.agregarAmigo(" "));
        assertThrows(IllegalArgumentException.class, () -> amistad.agregarAmigo(ana.getId()));
        amistad.agregarAmigo(beto.getId());
        assertThrows(IllegalArgumentException.class, () -> amistad.agregarAmigo(beto.getId()));
        assertEquals(1, amistad.contarAmigosDe(beto.getId()));
    }

    @Test void alFallarEscrituraConservaListasYContadores() throws Exception {
        usuarios.iniciarSesion("ana", "abc");
        amistad.agregarAmigo(beto.getId());
        Path archivo = directorio.resolve("amistades.json");
        Files.move(archivo, directorio.resolve("respaldo.json"));
        Files.createDirectory(archivo);
        Files.writeString(archivo.resolve("bloqueo"), "bloquea el reemplazo");
        assertThrows(IllegalStateException.class, () -> amistad.eliminarAmigo(beto.getId()));
        assertThrows(IllegalStateException.class, () -> amistad.agregarAmigo(cata.getId()));
        assertEquals(List.of(beto.getId()), ids(amistad.listarMisAmigos()));
        assertEquals(1, amistad.contarAmigosDe(ana.getId()));
        assertEquals(0, amistad.contarAmigosDe(cata.getId()));
    }

    private static List<String> ids(List<Usuario> perfiles) {
        return perfiles.stream().map(Usuario::getId).toList();
    }
}
