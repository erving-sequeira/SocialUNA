package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.model.entity.RelacionAmistad;
import java.util.ArrayList;

public class DocumentoAmistades {
    private ArrayList<RelacionAmistad> relaciones = new ArrayList<>();

    //Crea el documento con una lista de amistades vacía.
    public DocumentoAmistades() { }

    //Usa la lista de amistades recibida. Si llega null, empieza con una vacía.
    public DocumentoAmistades(ArrayList<RelacionAmistad> relaciones) {
        this.relaciones = relaciones == null ? new ArrayList<>() : relaciones;
    }

    //Devuelve la lista de amistades del documento.
    public ArrayList<RelacionAmistad> getRelaciones() { return relaciones; }

    //Cambia la lista de amistades por la recibida, o por una vacía si llega null.
    public void setRelaciones(ArrayList<RelacionAmistad> relaciones) {
        this.relaciones = relaciones == null ? new ArrayList<>() : relaciones;
    }
}
