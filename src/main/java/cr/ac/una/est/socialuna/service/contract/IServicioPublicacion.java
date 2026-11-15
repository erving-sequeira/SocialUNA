package cr.ac.una.est.socialuna.service.contract;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import java.util.ArrayList;

public interface IServicioPublicacion {

    //Recibe el texto, lo publica en el muro del usuario en sesión y devuelve la publicación.
    Publicacion publicar(String texto);

    //Recibe el texto y el tipo. Los guarda en el muro personal y devuelve la publicación.
    Publicacion publicar(String texto, TipoPublicacion tipo);

    //Devuelve las publicaciones del muro propio y de amigos, con las más nuevas primero.
    ArrayList<Publicacion> obtenerFeed();

}

