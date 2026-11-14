package cr.ac.una.est.socialuna.service.contract;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import java.util.ArrayList;

public interface IServicioPublicacion {

    //Recibe el texto, lo publica en el muro del usuario en sesión y devuelve la publicación.
    Publicacion publicar(String texto);

    //Devuelve las publicaciones del muro propio y de amigos, con las más nuevas primero.
    ArrayList<Publicacion> obtenerFeed();

}

