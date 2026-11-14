package cr.ac.una.est.socialuna.service.contract;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Comentario;
import java.util.ArrayList;

public interface IServicioComunidad {

    //Devuelve las publicaciones de la carrera del usuario. Si recibe un ID de sede, filtra por esa sede.
    ArrayList<Publicacion> obtenerPorCarrera(String sedeFiltroId);

    //Devuelve las publicaciones de la sede del usuario en sesión.
    ArrayList<Publicacion> obtenerPorSede();

    //Recibe el texto y el tipo, los guarda en la comunidad de carrera y devuelve la publicación.
    Publicacion publicarEnCarrera(String texto, TipoPublicacion tipo);

    //Recibe el texto y el tipo, los guarda en la comunidad de sede y devuelve la publicación.
    Publicacion publicarEnSede(String texto, TipoPublicacion tipo);

    //Recibe el ID de una publicación visible y el texto. Guarda y devuelve el comentario.
    Comentario comentar(String publicacionId, String texto);

    //Recibe el ID de una publicación visible y devuelve sus comentarios.
    ArrayList<Comentario> obtenerComentarios(String publicacionId);
}
