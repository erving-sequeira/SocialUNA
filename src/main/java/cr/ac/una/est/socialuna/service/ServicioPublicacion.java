
package cr.ac.una.est.socialuna.service;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Comentario;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.persistence.RepositorioPublicacionesJSON;
import cr.ac.una.est.socialuna.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;
import cr.ac.una.est.socialuna.service.contract.IServicioPublicacion;

public class ServicioPublicacion implements IServicioPublicacion {

    private IServicioUsuarios servicioUsuarios;
    private IServicioAmistad servicioAmistad;
    private RepositorioPublicacionesJSON repositorioPublicaciones;

    //Recibe los servicios de usuarios y amistades junto con el repositorio de publicaciones.
    public ServicioPublicacion(
            IServicioUsuarios servicioUsuarios,
            IServicioAmistad servicioAmistad,
            RepositorioPublicacionesJSON repositorioPublicaciones) {

        if (servicioUsuarios == null || servicioAmistad == null
                || repositorioPublicaciones == null) {
            throw new IllegalArgumentException(
                    "Los servicios y el repositorio son obligatorios.");
        }

        this.servicioUsuarios = servicioUsuarios;
        this.servicioAmistad = servicioAmistad;
        this.repositorioPublicaciones = repositorioPublicaciones;
    }

    //Recibe el texto, lo guarda como post general en el muro del usuario y devuelve el post.
    @Override
    public Publicacion publicar(String texto) {
        return publicar(texto, TipoPublicacion.GENERAL);
    }

    //Recibe el texto y el tipo. Crea, guarda y devuelve la publicación del muro.
    @Override
    public Publicacion publicar(String texto, TipoPublicacion tipo) {
        Usuario usuario = exigirSesion();

        Publicacion publicacion = new Publicacion(
                UUID.randomUUID().toString(),
                usuario.getId(),
                validarTexto(texto),
                System.currentTimeMillis(),
                AmbitoPublicacion.MURO,
                tipo,
                null,
                null
        );

        repositorioPublicaciones.guardar(publicacion);
        return publicacion;
    }

    //Devuelve los posts del muro propio y de amigos, con los más nuevos primero.
    @Override
    public ArrayList<Publicacion> obtenerFeed() {

        Usuario usuario = exigirSesion();

        ArrayList<Publicacion> todas =
                repositorioPublicaciones.listar();

        ArrayList<Usuario> amigos =
                servicioAmistad.listarMisAmigos();

        ArrayList<Publicacion> feed = new ArrayList<>();

        for (Publicacion publicacion : todas) {

            if (publicacion.getAmbito() != AmbitoPublicacion.MURO) {
                continue;
            }

            boolean esPropia =
                    publicacion.getAutorId().equals(usuario.getId());

            boolean esDeUnAmigo = false;

            for (Usuario amigo : amigos) {
                if (publicacion.getAutorId().equals(amigo.getId())) {
                    esDeUnAmigo = true;
                    break;
                }
            }

            if (esPropia || esDeUnAmigo) {
                feed.add(publicacion);
            }
        }

        return ordenarRecientes(feed);
    }

    //Recibe el ID de un post visible y el texto. Guarda y devuelve un comentario raíz.
    @Override
    public Comentario comentar(String publicacionId, String texto) {
        return responder(publicacionId, null, texto);
    }

    //Recibe el post, el comentario padre y el texto. Normaliza las respuestas a un solo nivel.
    @Override
    public Comentario responder(String publicacionId, String comentarioPadreId, String texto) {
        Usuario usuario = exigirSesion();
        Publicacion publicacion = exigirPublicacionVisible(publicacionId, usuario);

        Comentario comentario = new Comentario(
                UUID.randomUUID().toString(),
                usuario.getId(),
                validarTexto(texto),
                System.currentTimeMillis(),
                normalizarPadre(publicacion, comentarioPadreId)
        );

        repositorioPublicaciones.agregarComentario(publicacion.getId(), comentario);
        return comentario;
    }

    //Recibe el ID de un post visible y devuelve sus comentarios.
    @Override
    public ArrayList<Comentario> obtenerComentarios(String publicacionId) {
        Usuario usuario = exigirSesion();
        return exigirPublicacionVisible(publicacionId, usuario).getComentarios();
    }

    //Recibe el ID del post y del comentario propio. Borra el comentario con todas sus respuestas.
    @Override
    public void eliminarComentario(String publicacionId, String comentarioId) {
        Usuario usuario = exigirSesion();
        Publicacion publicacion = exigirPublicacionVisible(publicacionId, usuario);
        Comentario comentario = exigirComentarioPropio(publicacion, comentarioId, usuario);
        repositorioPublicaciones.eliminarComentario(publicacion.getId(), comentario.getId());
    }

    //Recibe el ID de un post visible y alterna el me gusta del usuario en sesión.
    @Override
    public Publicacion alternarReaccion(String publicacionId) {
        Usuario usuario = exigirSesion();
        Publicacion publicacion = exigirPublicacionVisible(publicacionId, usuario);
        repositorioPublicaciones.alternarReaccion(publicacion.getId(), usuario.getId());
        return publicacion;
    }

    //Recibe el ID de un usuario y devuelve sus publicaciones de muro visibles para quien mira.
    @Override
    public ArrayList<Publicacion> obtenerPublicacionesDe(String usuarioId) {
        Usuario actual = exigirSesion();
        String id = validarTexto(usuarioId);
        ArrayList<Publicacion> resultado = new ArrayList<>();

        for (Publicacion publicacion : repositorioPublicaciones.listar()) {
            if (publicacion.getAmbito() != AmbitoPublicacion.MURO
                    || !publicacion.getAutorId().equals(id)) {
                continue;
            }
            if (publicacion.getAutorId().equals(actual.getId()) || esAmigo(publicacion.getAutorId())) {
                resultado.add(publicacion);
            }
        }

        return ordenarRecientes(resultado);
    }

    //Recibe el ID de una publicación propia del muro y la elimina con sus comentarios.
    @Override
    public void eliminarPublicacion(String publicacionId) {
        Usuario usuario = exigirSesion();
        String id = validarTexto(publicacionId);
        Publicacion publicacion = repositorioPublicaciones.buscarPorId(id);

        if (publicacion == null) {
            throw new IllegalArgumentException("No existe la publicación indicada.");
        }
        if (publicacion.getAmbito() != AmbitoPublicacion.MURO) {
            throw new IllegalArgumentException("La publicación no pertenece al muro.");
        }
        if (!publicacion.getAutorId().equals(usuario.getId())) {
            throw new IllegalStateException("Solo el autor puede eliminar esta publicación.");
        }

        repositorioPublicaciones.eliminar(id);
    }

    //Devuelve el post si es del muro propio o de un amigo. Si no, da error.
    private Publicacion exigirPublicacionVisible(String publicacionId, Usuario usuario) {
        String id = validarTexto(publicacionId);
        Publicacion publicacion = repositorioPublicaciones.buscarPorId(id);

        if (publicacion == null) {
            throw new IllegalArgumentException("No existe la publicación indicada.");
        }
        if (publicacion.getAmbito() != AmbitoPublicacion.MURO) {
            throw new IllegalArgumentException("La publicación no está disponible para este usuario.");
        }
        if (publicacion.getAutorId().equals(usuario.getId()) || esAmigo(publicacion.getAutorId())) {
            return publicacion;
        }
        throw new IllegalArgumentException("La publicación no está disponible para este usuario.");
    }

    //Recibe el ID de un comentario y el usuario. Devuelve el comentario si pertenece a ese usuario.
    private Comentario exigirComentarioPropio(Publicacion publicacion, String comentarioId, Usuario usuario) {
        String id = validarTexto(comentarioId);

        for (Comentario comentario : publicacion.getComentarios()) {
            if (comentario.getId().equals(id)) {
                if (!comentario.getAutorId().equals(usuario.getId())) {
                    throw new IllegalStateException("Solo el autor del comentario puede eliminarlo.");
                }
                return comentario;
            }
        }

        throw new IllegalArgumentException("No existe el comentario indicado.");
    }

    //Recibe el ID de un autor y dice si es amigo del usuario en sesión.
    private boolean esAmigo(String autorId) {
        for (Usuario amigo : servicioAmistad.listarMisAmigos()) {
            if (amigo.getId().equals(autorId)) return true;
        }
        return false;
    }

    //Recibe el post y el padre elegido. Devuelve el comentario raíz del hilo, o null si no hay padre.
    private String normalizarPadre(Publicacion publicacion, String comentarioPadreId) {
        if (comentarioPadreId == null || comentarioPadreId.isBlank()) {
            return null;
        }

        String id = comentarioPadreId.strip();

        for (Comentario comentario : publicacion.getComentarios()) {
            if (comentario.getId().equals(id)) {
                return comentario.getRespuestaAId() == null
                        ? comentario.getId()
                        : comentario.getRespuestaAId();
            }
        }

        throw new IllegalArgumentException("No existe el comentario indicado.");
    }

    //Devuelve el usuario que entró. Da error si se intenta usar el muro sin sesión.
    private Usuario exigirSesion() {

        Usuario usuario = servicioUsuarios.getUsuarioActual();

        if (usuario == null) {
            throw new IllegalStateException(
                    "Debe iniciar sesión para realizar esta operación.");
        }

        return usuario;
    }

    //Revisa que el texto recibido tenga contenido. Devuelve el texto sin espacios en los bordes.
    private String validarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException(
                    "El texto de la publicación no puede estar vacío.");
        }
        return texto.strip();
    }

    //Recibe una lista y devuelve una copia ordenada de más nueva a más vieja. En empates usa el ID.
    private ArrayList<Publicacion> ordenarRecientes(
            ArrayList<Publicacion> publicaciones) {

        if (publicaciones == null) {
            throw new IllegalArgumentException(
                    "La lista de publicaciones es obligatoria.");
        }
        ArrayList<Publicacion> ordenadas =
                new ArrayList<>(publicaciones);

        ordenadas.sort(
                Comparator.comparingLong(Publicacion::getCreadoEn)
                        .reversed()
                        .thenComparing(Publicacion::getId)
        );

        return ordenadas;
    }
}



