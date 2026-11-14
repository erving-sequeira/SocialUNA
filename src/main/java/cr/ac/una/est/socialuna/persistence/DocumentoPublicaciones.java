
package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.model.entity.Publicacion;
import java.util.ArrayList;

public class DocumentoPublicaciones {

    private ArrayList<Publicacion> publicaciones;

    //Crea el documento con una lista de publicaciones vacía.
    public DocumentoPublicaciones() {
        this.publicaciones = new ArrayList<>();
    }

    //Usa la lista de publicaciones recibida. Si llega null, empieza con una vacía.
    public DocumentoPublicaciones(ArrayList<Publicacion> publicaciones) {
        this.publicaciones = publicaciones == null
                ? new ArrayList<>()
                : publicaciones;
    }

    //Devuelve la lista de publicaciones del documento.
    public ArrayList<Publicacion> getPublicaciones() {
        return publicaciones;
    }
    //Cambia la lista de publicaciones por la recibida, o por una vacía si llega null.
    public void setPublicaciones(ArrayList<Publicacion> publicaciones) {
        this.publicaciones = publicaciones == null
                ? new ArrayList<>()
                : publicaciones;
    }
}
