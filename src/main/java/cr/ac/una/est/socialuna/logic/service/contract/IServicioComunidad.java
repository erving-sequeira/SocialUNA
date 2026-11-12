package cr.ac.una.est.socialuna.logic.service.contract;
import cr.ac.una.est.socialuna.logic.model.entity.Publicacion;
import cr.ac.una.est.socialuna.logic.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.logic.model.entity.Comentario;
import java.util.ArrayList;

public interface IServicioComunidad {

    //obtiene publicaciones de una carrera
    ArrayList<Publicacion> obtenerPorCarrera(String sedeFiltroId);

    //obtiene publicaciones de la sede configurada
    ArrayList<Publicacion> obtenerPorSede();

    //publica en la carrera configurada
    Publicacion publicarEnCarrera(String texto, TipoPublicacion tipo);

    //publica en la sede configurada
    Publicacion publicarEnSede(String texto, TipoPublicacion tipo);

    //agrega un comentario a una publicación
    Comentario comentar(String publicacionId, String texto);

    //obtiene los comentarios de una publicación
    ArrayList<Comentario> obtenerComentarios(String publicacionId);
}