package cr.ac.una.est.socialuna.service;
import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Comentario;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.contract.IServicioComunidad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.persistence.RepositorioPublicacionesJSON;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

public class ServicioComunidad implements IServicioComunidad {

    private final IServicioUsuarios servicioUsuarios;

    private final RepositorioPublicacionesJSON repositorioPublicaciones;

    //Recibe el servicio de usuarios y el repositorio de publicaciones para atender las dos comunidades.
    public ServicioComunidad(
            IServicioUsuarios servicioUsuarios,
            RepositorioPublicacionesJSON repositorioPublicaciones) {
        if (servicioUsuarios == null) {
            throw new IllegalArgumentException("El servicio de usuarios es obligatorio.");
        }
        if (repositorioPublicaciones == null) {
            throw new IllegalArgumentException("El repositorio de publicaciones es obligatorio.");
        }
        this.servicioUsuarios = servicioUsuarios;
        this.repositorioPublicaciones = repositorioPublicaciones;
    }

    //Devuelve los posts de la carrera, con los más nuevos primero. Con un ID de sede, filtra por esa sede.
    @Override
    public ArrayList<Publicacion> obtenerPorCarrera(String sedeFiltroId) {
        Usuario usuario = exigirSesion();
        exigirDatoAcademico(usuario.getCarreraId(), "carrera");

        ArrayList<Publicacion> resultado = new ArrayList<>();

        String filtro = sedeFiltroId == null || sedeFiltroId.isBlank() ? null : sedeFiltroId.strip();

        for (Publicacion publicacion : repositorioPublicaciones.listar()) {
            boolean mismaCarrera = publicacion.getAmbito() == AmbitoPublicacion.CARRERA && usuario.getCarreraId().equals(publicacion.getCarreraId());

            boolean sedeCoincide = filtro == null || filtro.equals(publicacion.getSedeId());

            if (mismaCarrera && sedeCoincide) {
                resultado.add(publicacion);
            }
        }

        return ordenarRecientes(resultado);
    }

    //Devuelve los posts de la sede del usuario, de más nuevos a más viejos.
    @Override
    public ArrayList<Publicacion> obtenerPorSede() {
        Usuario usuario = exigirSesion();
        exigirDatoAcademico(usuario.getSedeId(), "sede");

        ArrayList<Publicacion> resultado = new ArrayList<>();

        for (Publicacion publicacion : repositorioPublicaciones.listar()) {
            boolean mismaSede = publicacion.getAmbito() == AmbitoPublicacion.SEDE && usuario.getSedeId().equals(publicacion.getSedeId());
            if (mismaSede) {
                resultado.add(publicacion);
            }
        }
        return ordenarRecientes(resultado);
    }

    //Recibe texto y tipo, guarda el post en la carrera del usuario y lo devuelve con la sede de origen.
    @Override
    public Publicacion publicarEnCarrera(String texto, TipoPublicacion tipo) {
        Usuario usuario = exigirSesion();

        exigirDatoAcademico(usuario.getCarreraId(), "carrera");

        Publicacion publicacion = new Publicacion(
                UUID.randomUUID().toString(),
                usuario.getId(),
                validarTexto(texto),
                System.currentTimeMillis(),
                AmbitoPublicacion.CARRERA,
                validarTipo(tipo),
                usuario.getCarreraId(),
                usuario.getSedeId()
        );
        repositorioPublicaciones.guardar(publicacion);
        return publicacion;
    }

    //Recibe texto y tipo, guarda el post en la sede del usuario y lo devuelve.
    @Override
    public Publicacion publicarEnSede(String texto, TipoPublicacion tipo) {
        Usuario usuario = exigirSesion();

        exigirDatoAcademico(usuario.getSedeId(), "sede");

        Publicacion publicacion = new Publicacion(
                UUID.randomUUID().toString(),
                usuario.getId(),
                validarTexto(texto),
                System.currentTimeMillis(),
                AmbitoPublicacion.SEDE,
                validarTipo(tipo),
                usuario.getCarreraId(),
                usuario.getSedeId()
        );
        repositorioPublicaciones.guardar(publicacion);
        return publicacion;
    }

    //Recibe el ID del post y el texto. Si el usuario puede ver el post, guarda y devuelve el comentario.
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

    //Recibe el ID del post y devuelve sus comentarios si el usuario puede verlo.
    @Override
    public ArrayList<Comentario> obtenerComentarios(String publicacionId) {
        Usuario usuario = exigirSesion();
        Publicacion publicacion = exigirPublicacionVisible(publicacionId, usuario);
        return publicacion.getComentarios();
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

    //Recibe el ID de una publicación propia y la elimina con todos sus comentarios.
    @Override
    public void eliminarPublicacion(String publicacionId) {
        Usuario usuario = exigirSesion();
        Publicacion publicacion = exigirPublicacionVisible(publicacionId, usuario);

        if (!publicacion.getAutorId().equals(usuario.getId())) {
            throw new IllegalStateException("Solo el autor puede eliminar esta publicación.");
        }

        repositorioPublicaciones.eliminar(publicacion.getId());
    }

    //Devuelve el usuario que entró. Da error si se intenta usar la comunidad sin sesión.
    private Usuario exigirSesion() {
        Usuario usuario = servicioUsuarios.getUsuarioActual();
        if (usuario == null) {
            throw new IllegalStateException("Debe iniciar sesión para usar la comunidad.");
        }
        return usuario;
    }

    //Recibe el texto y lo devuelve sin espacios en los bordes. Da error si venía vacío.
    private String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto es obligatorio.");
        }
        return texto.strip();
    }

    //Recibe el ID y el nombre del dato. Da error si falta la carrera o la sede que se pide.
    private void exigirDatoAcademico(String id, String dato) {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Configura tu " + dato + " para usar esta comunidad.");
        }
    }

    //Devuelve el tipo recibido o da error si no se eligió ninguno.
    private TipoPublicacion validarTipo(TipoPublicacion tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de publicación es obligatorio.");
        }
        return tipo;
    }

    //Recibe el ID del post y el usuario. Devuelve el post si es de su carrera o sede.
    private Publicacion exigirPublicacionVisible(String publicacionId, Usuario usuario) {
        String id = validarTexto(publicacionId);
        Publicacion publicacion = repositorioPublicaciones.buscarPorId(id);

        if (publicacion == null) {
            throw new IllegalArgumentException("No existe la publicación indicada.");
        }

        boolean visibleEnCarrera = publicacion.getAmbito() == AmbitoPublicacion.CARRERA && usuario.getCarreraId() != null && usuario.getCarreraId().equals(publicacion.getCarreraId());

        boolean visibleEnSede = publicacion.getAmbito() == AmbitoPublicacion.SEDE && usuario.getSedeId() != null && usuario.getSedeId().equals(publicacion.getSedeId());

        if (!visibleEnCarrera && !visibleEnSede) {
            throw new IllegalArgumentException("La publicación no está disponible para este usuario.");
        }
        return publicacion;
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

    //Ordena la lista recibida de más nueva a más vieja y la devuelve. En empates usa el ID.
    private ArrayList<Publicacion> ordenarRecientes(ArrayList<Publicacion> publicaciones) {
        publicaciones.sort(Comparator.comparingLong(Publicacion::getCreadoEn).reversed()
                .thenComparing(Publicacion::getId));
        return publicaciones;
    }
}
