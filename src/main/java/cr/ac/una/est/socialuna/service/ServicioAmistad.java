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

    //Recibe el servicio de usuarios, los repositorios cargados y el grafo. Deja listas las conexiones.
    public ServicioAmistad(IServicioUsuarios servicioUsuarios, RepositorioUsuariosJSON repositorioUsuarios,
                           RepositorioAmistadesJSON repositorioAmistades, GrafoAmigos grafo) {
        this.servicioUsuarios = Objects.requireNonNull(servicioUsuarios);
        this.repositorioUsuarios = Objects.requireNonNull(repositorioUsuarios);
        this.repositorioAmistades = Objects.requireNonNull(repositorioAmistades);
        this.grafo = Objects.requireNonNull(grafo);
        reconstruirGrafo();
    }

    //Vuelve a armar el grafo con los perfiles y amistades guardados. Salta las relaciones con perfiles que ya no están.
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

    //Recibe el ID del otro perfil, guarda la amistad con el usuario actual y actualiza el grafo.
    @Override
    public void agregarAmigo(String amigoId) {
        String propioId = exigirSesion().getId();
        String destinoId = exigirPerfil(amigoId).getId();
        var relacion = new RelacionAmistad(UUID.randomUUID().toString(), propioId, destinoId, System.currentTimeMillis());
        repositorioAmistades.guardar(relacion);
        reconstruirGrafo();
    }

    //Recibe el ID del otro perfil, borra la amistad del usuario actual y actualiza el grafo.
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

    //Devuelve los perfiles de los amigos del usuario en sesión.
    @Override
    public ArrayList<Usuario> listarMisAmigos() {
        return listarVecinos(exigirSesion().getId());
    }

    //Con una sesión activa, recibe el ID de un perfil y devuelve los perfiles de sus amigos.
    @Override
    public ArrayList<Usuario> listarAmigosDe(String perfilId) {
        exigirSesion();
        return listarVecinos(exigirPerfil(perfilId).getId());
    }

    //Con una sesión activa, recibe el ID de un perfil y devuelve su número de amigos.
    @Override
    public int contarAmigosDe(String perfilId) {
        exigirSesion();
        return grafo.obtenerAmigos(exigirPerfil(perfilId).getId()).size();
    }

    //Con una sesión activa, recibe dos IDs y devuelve true si están conectados en el grafo.
    @Override
    public boolean sonAmigos(String usuarioId, String amigoId) {
        exigirSesion();
        return grafo.sonAmigos(usuarioId, amigoId);
    }

    //Recibe un ID, busca sus amigos en el grafo y devuelve los perfiles que todavía existen.
    private ArrayList<Usuario> listarVecinos(String perfilId) {
        var amigos = new ArrayList<Usuario>();
        for (String id : grafo.obtenerAmigos(perfilId)) {
            Usuario amigo = repositorioUsuarios.buscarUsuarioPorId(id);
            if (amigo != null) amigos.add(amigo);
        }
        return amigos;
    }

    //Recibe un ID y devuelve el perfil. Da error si falta el ID o el perfil no existe.
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

    //Devuelve el usuario en sesión. Da error si nadie ha entrado.
    private Usuario exigirSesion() {
        Usuario actual = servicioUsuarios.getUsuarioActual();
        if (actual == null) {
            throw new IllegalStateException("Debes iniciar sesión para usar las amistades.");
        }
        return actual;
    }
}
