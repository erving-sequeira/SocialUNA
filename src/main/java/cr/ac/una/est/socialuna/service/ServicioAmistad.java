package cr.ac.una.est.socialuna.service;

import cr.ac.una.est.socialuna.model.entity.RelacionAmistad;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.model.graph.GrafoAmigos;
import cr.ac.una.est.socialuna.persistence.RepositorioAmistadesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioUsuariosJSON;
import cr.ac.una.est.socialuna.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class ServicioAmistad implements IServicioAmistad {
    private final IServicioUsuarios servicioUsuarios;
    private final RepositorioUsuariosJSON repositorioUsuarios;
    private final RepositorioAmistadesJSON repositorioAmistades;
    private final GrafoAmigos grafo;

    /** Recibe los repositorios con sus datos ya cargados. */
    public ServicioAmistad(IServicioUsuarios servicioUsuarios, RepositorioUsuariosJSON repositorioUsuarios,
                           RepositorioAmistadesJSON repositorioAmistades, GrafoAmigos grafo) {
        this.servicioUsuarios = Objects.requireNonNull(servicioUsuarios);
        this.repositorioUsuarios = Objects.requireNonNull(repositorioUsuarios);
        this.repositorioAmistades = Objects.requireNonNull(repositorioAmistades);
        this.grafo = Objects.requireNonNull(grafo);
        reconstruirGrafo();
    }

    public void reconstruirGrafo() {
        var relaciones = repositorioAmistades.listar();
        var perfiles = repositorioUsuarios.listarUsuarios();
        var ids = new HashSet<String>();
        grafo.vaciar();
        for (Usuario perfil : perfiles) {
            ids.add(perfil.getId());
            grafo.agregarUsuario(perfil.getId());
        }
        for (RelacionAmistad relacion : relaciones) {
            String primero = relacion.getUsuarioAId();
            String segundo = relacion.getUsuarioBId();
            if (ids.contains(primero) && ids.contains(segundo)) {
                grafo.conectar(primero, segundo);
            }
        }
    }

    @Override
    public void agregarAmigo(String amigoId) {
        String propioId = exigirSesion().getId();
        String destinoId = exigirPerfil(amigoId).getId();
        var relacion = new RelacionAmistad(UUID.randomUUID().toString(), propioId, destinoId, System.currentTimeMillis());
        repositorioAmistades.guardar(relacion);
        reconstruirGrafo();
    }

    @Override
    public void eliminarAmigo(String amigoId) {
        String propioId = exigirSesion().getId();
        String destinoId = exigirPerfil(amigoId).getId();
        RelacionAmistad relacion = repositorioAmistades.buscarEntre(propioId, destinoId);
        if (relacion == null) {
            throw new IllegalArgumentException("No existe una amistad con ese perfil.");
        }
        repositorioAmistades.eliminar(relacion.getId());
        reconstruirGrafo();
    }

    @Override
    public ArrayList<Usuario> listarMisAmigos() {
        return listarVecinos(exigirSesion().getId());
    }

    @Override
    public ArrayList<Usuario> listarAmigosDe(String perfilId) {
        exigirSesion();
        return listarVecinos(exigirPerfil(perfilId).getId());
    }

    @Override
    public int contarAmigosDe(String perfilId) {
        exigirSesion();
        return grafo.obtenerAmigos(exigirPerfil(perfilId).getId()).size();
    }

    @Override
    public boolean sonAmigos(String usuarioId, String amigoId) {
        exigirSesion();
        return grafo.sonAmigos(usuarioId, amigoId);
    }

    private ArrayList<Usuario> listarVecinos(String perfilId) {
        var amigos = new ArrayList<Usuario>();
        for (String id : grafo.obtenerAmigos(perfilId)) {
            Usuario amigo = repositorioUsuarios.buscarUsuarioPorId(id);
            if (amigo != null) amigos.add(amigo);
        }
        return amigos;
    }

    private Usuario exigirPerfil(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("El ID del perfil es obligatorio.");
        }
        Usuario perfil = repositorioUsuarios.buscarUsuarioPorId(id);
        if (perfil == null) {
            throw new IllegalArgumentException("No se encontró el perfil.");
        }
        return perfil;
    }

    private Usuario exigirSesion() {
        Usuario actual = servicioUsuarios.getUsuarioActual();
        if (actual == null) {
            throw new IllegalStateException("Debes iniciar sesión para usar las amistades.");
        }
        return actual;
    }
}
