package cr.ac.una.est.socialuna.service.contract;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import java.util.ArrayList;

public interface IServicioPublicacion {

    Publicacion publicar(String texto);

    ArrayList<Publicacion> obtenerFeed();

}

