
package cr.ac.una.est.socialuna.model.entity;

import java.util.ArrayList;

public class Publicacion {
    // Atributos
    private String id;
    private String autorId;
    private String texto;
    private long creadoEn;
    private AmbitoPublicacion ambito;
    private TipoPublicacion tipo;
    private String carreraId;
    private String sedeId;
    private ArrayList<Comentario> comentarios;

    // Constructor protegido sin parametros
    protected Publicacion() {
        this.comentarios = new ArrayList<>();
    }

    // Constructor publico
    public Publicacion(String id, String autorId, String texto,
                       long creadoEn, AmbitoPublicacion ambito,
                       TipoPublicacion tipo, String carreraId,
                       String sedeId) {

        this.id = id;
        this.autorId = autorId;
        this.texto = texto;
        this.creadoEn = creadoEn;
        this.ambito = ambito;
        this.tipo = tipo;
        this.carreraId = carreraId;
        this.sedeId = sedeId;
        this.comentarios = new ArrayList<>();
    }

    // Metodo para agregar comentarios
    public void agregarComentario(Comentario comentario) {
        this.comentarios.add(comentario);
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getAutorId() {
        return autorId;
    }

    public String getTexto() {
        return texto;
    }

    public long getCreadoEn() {
        return creadoEn;
    }

    public AmbitoPublicacion getAmbito() {
        return ambito;
    }

    public TipoPublicacion getTipo() {
        return tipo;
    }

    public String getCarreraId() {
        return carreraId;
    }

    public String getSedeId() {
        return sedeId;
    }

    public ArrayList<Comentario> getComentarios() {
        return new ArrayList<>(comentarios);
    }
}
