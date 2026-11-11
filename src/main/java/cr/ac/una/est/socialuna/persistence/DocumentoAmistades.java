package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.model.entity.RelacionAmistad;
import java.util.ArrayList;

/** Documento raíz de amistades.json: una relación por par de usuarios. */
public class DocumentoAmistades {
    private ArrayList<RelacionAmistad> relaciones = new ArrayList<>();

    public DocumentoAmistades() { }

    public DocumentoAmistades(ArrayList<RelacionAmistad> relaciones) {
        this.relaciones = relaciones == null ? new ArrayList<>() : relaciones;
    }

    public ArrayList<RelacionAmistad> getRelaciones() { return relaciones; }

    public void setRelaciones(ArrayList<RelacionAmistad> relaciones) {
        this.relaciones = relaciones == null ? new ArrayList<>() : relaciones;
    }
}
