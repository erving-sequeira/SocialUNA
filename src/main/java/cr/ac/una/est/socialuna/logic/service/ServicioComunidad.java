package cr.ac.una.est.socialuna.logic.service;
import cr.ac.una.est.socialuna.logic.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.logic.model.entity.Comentario;
import cr.ac.una.est.socialuna.logic.model.entity.Publicacion;
import cr.ac.una.est.socialuna.logic.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.logic.model.entity.Usuario;
import cr.ac.una.est.socialuna.logic.service.contract.IServicioComunidad;
import cr.ac.una.est.socialuna.logic.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.persistence.RepositorioPublicacionesJSON;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.UUID;

//coordina las operaciones de publicaciones y comentarios, consulta las publicaciones, verifica la sesión del usuario
//y aplica las reglas para publicar o consultar contenido de una carrera o sede
public class ServicioComunidad implements IServicioComunidad {

    //servicio que permite consultar al usuario con la sesión activa
    private final IServicioUsuarios servicioUsuarios;

    //repositorio que carga, consulta y guarda publicaciones y comentarios
    private final RepositorioPublicacionesJSON repositorioPublicaciones;

    //crea el servicio y verifica que reciba sus dependencias
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

    //obtiene las publicaciones de la carrera del usuario con sesión activa
    //si se especifica una sede, limita los resultados a esa sede
    @Override
    public ArrayList<Publicacion> obtenerPorCarrera(String sedeFiltroId) {
        Usuario usuario = exigirSesion();
        //para consultar por carrera, el usuario debe tener una carrera configurada.
        exigirDatoAcademico(usuario.getCarreraId(), "carrera");

        ArrayList<Publicacion> resultado = new ArrayList<>();

        String filtro = sedeFiltroId == null || sedeFiltroId.isBlank() ? null : sedeFiltroId.strip();

        //recorre las publicaciones almacenadas y conserva las de la carrera del usuario
        for (Publicacion publicacion : repositorioPublicaciones.listar()) {
            boolean mismaCarrera = publicacion.getAmbito() == AmbitoPublicacion.CARRERA && usuario.getCarreraId().equals(publicacion.getCarreraId());

            //un filtro null significa que se incluyen todas las sedes de la carrera.
            boolean sedeCoincide = filtro == null || filtro.equals(publicacion.getSedeId());

            if (mismaCarrera && sedeCoincide) {
                resultado.add(publicacion);
            }
        }

        //ordena los resultados para mostrar primero los más recientes
        return ordenarRecientes(resultado);
    }

    //obtiene las publicaciones de la sede configurada en el perfil del usuario
    @Override
    public ArrayList<Publicacion> obtenerPorSede() {
        Usuario usuario = exigirSesion();
        //para consultar por sede, el usuario debe tener una sede configurada
        exigirDatoAcademico(usuario.getSedeId(), "sede");

        ArrayList<Publicacion> resultado = new ArrayList<>();

        //conserva las publicaciones cuyo ámbito es sede y coincide con el usuario
        for (Publicacion publicacion : repositorioPublicaciones.listar()) {
            boolean mismaSede = publicacion.getAmbito() == AmbitoPublicacion.SEDE && usuario.getSedeId().equals(publicacion.getSedeId());
            if (mismaSede) {
                resultado.add(publicacion);
            }
        }
        return ordenarRecientes(resultado);
    }

    //crea una publicación para la carrera configurada en el perfil del usuario
    @Override
    public Publicacion publicarEnCarrera(String texto, TipoPublicacion tipo) {
        Usuario usuario = exigirSesion();

        //impide publicar en una carrera si el perfil no tiene carrera configurada
        exigirDatoAcademico(usuario.getCarreraId(), "carrera");

        //conserva la sede de origen para el filtro de la comunidad de carrera
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
        //el repositorio agrega la publicación y persiste el cambio
        repositorioPublicaciones.guardar(publicacion);
        return publicacion;
    }

    //crea una publicación para la sede configurada en el perfil del usuario
    @Override
    public Publicacion publicarEnSede(String texto, TipoPublicacion tipo) {
        Usuario usuario = exigirSesion();

        //impide publicar en una sede si el perfil no tiene sede configurada
        exigirDatoAcademico(usuario.getSedeId(), "sede");

        //crea la publicación con el ámbito y los datos académicos del usuario
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

    //agrega un comentario a una publicación visible para el usuario actual
    @Override
    public Comentario comentar(String publicacionId, String texto) {
        Usuario usuario = exigirSesion();

        //busca la publicación y verifica que pertenezca a un ámbito visible
        Publicacion publicacion = exigirPublicacionVisible(publicacionId, usuario);

        //crea el comentario con el usuario actual y la hora en milisegundos
        Comentario comentario = new Comentario(
                UUID.randomUUID().toString(),
                usuario.getId(),
                validarTexto(texto),
                System.currentTimeMillis()
        );
        //agrega el comentario a la publicación y guarda el documento actualizado
        repositorioPublicaciones.agregarComentario(publicacion.getId(), comentario);
        return comentario;
    }

    //devuelve los comentarios de una publicación visible para el usuario actual
    @Override
    public ArrayList<Comentario> obtenerComentarios(String publicacionId) {
        Usuario usuario = exigirSesion();
        //no permite consultar comentarios de una publicación fuera de su ámbito
        Publicacion publicacion = exigirPublicacionVisible(publicacionId, usuario);
        return publicacion.getComentarios();
    }

    //obtiene el usuario con sesión activa
    private Usuario exigirSesion() {
        Usuario usuario = servicioUsuarios.getUsuarioActual();
        if (usuario == null) {
            throw new IllegalStateException("Debe iniciar sesión para usar la comunidad.");
        }
        return usuario;
    }

    //comprueba que un texto no sea null ni esté vacío y elimina espacios externos
    private String validarTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto es obligatorio.");
        }
        return texto.strip();
    }

    private void exigirDatoAcademico(String id, String dato) {
        if (id == null || id.isBlank()) {
            throw new IllegalStateException("Configura tu " + dato + " para usar esta comunidad.");
        }
    }

    //comprueba que se haya seleccionado un tipo de publicación
    private TipoPublicacion validarTipo(TipoPublicacion tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de publicación es obligatorio.");
        }
        return tipo;
    }

    //busca una publicación y comprueba que sea visible para el usuario
    //una publicación de carrera es visible si coincide la carrera
    //una publicación de sede es visible si coincide la sede
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

    //ordena las publicaciones por fecha, de la más reciente a la más antigua
    private ArrayList<Publicacion> ordenarRecientes(ArrayList<Publicacion> publicaciones) {
        publicaciones.sort(Comparator.comparingLong(Publicacion::getCreadoEn).reversed()
                .thenComparing(Publicacion::getId));
        return publicaciones;
    }
}