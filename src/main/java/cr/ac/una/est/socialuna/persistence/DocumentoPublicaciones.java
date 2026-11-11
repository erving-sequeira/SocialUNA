
package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.logic.model.entity.Publicacion;
import java.util.ArrayList;

public class DocumentoPublicaciones {

    private ArrayList<Publicacion> publicaciones;

    public DocumentoPublicaciones() {
        this.publicaciones = new ArrayList<>();
    }

    public DocumentoPublicaciones(ArrayList<Publicacion> publicaciones) {
        this.publicaciones = publicaciones == null
                ? new ArrayList<>()
                : publicaciones;
    }

    public ArrayList<Publicacion> getPublicaciones() {
        return publicaciones;
    }
    public void setPublicaciones(ArrayList<Publicacion> publicaciones) {
        this.publicaciones = publicaciones == null
                ? new ArrayList<>()
                : publicaciones;
    }
}
