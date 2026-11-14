
package cr.ac.una.est.socialuna.model.entity;

import java.util.ArrayList;

public class Publicacion {
    private String id;
    private String autorId;
    private String texto;
    private long creadoEn;
    private AmbitoPublicacion ambito;
    private TipoPublicacion tipo;
    private String carreraId;
    private String sedeId;
    private ArrayList<Comentario> comentarios;

    //Deja la lista de comentarios vacía al crear la publicación desde el JSON.
    protected Publicacion() {
        this.comentarios = new ArrayList<>();
    }

    //Crea la publicación con ID, autor, texto, fecha, ámbito y tipo. Pide carrera o sede según el ámbito.
    public Publicacion(String id, String autorId, String texto,
                       long creadoEn, AmbitoPublicacion ambito,
                       TipoPublicacion tipo, String carreraId,
                       String sedeId) {

        if (id == null || id.isBlank() || autorId == null || autorId.isBlank()
                || texto == null || texto.isBlank() || creadoEn < 0 || ambito == null || tipo == null) {
            throw new IllegalArgumentException(
                    "La publicación requiere ID, autor, texto, fecha, ámbito y tipo válidos.");
        }
        if (ambito == AmbitoPublicacion.CARRERA && (carreraId == null || carreraId.isBlank())) {
            throw new IllegalArgumentException(
                    "Una publicación de carrera requiere su carrera.");
        }
        if (ambito == AmbitoPublicacion.SEDE && (sedeId == null || sedeId.isBlank())) {
            throw new IllegalArgumentException(
                    "Una publicación de sede requiere su sede.");
        }

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

    //Recibe un comentario y lo agrega a la lista de esta publicación.
    public void agregarComentario(Comentario comentario) {
        if (comentario == null) {
            throw new IllegalArgumentException(
                    "El comentario es obligatorio.");
        }
        this.comentarios.add(comentario);
    }

    //Devuelve el ID de la publicación.
    public String getId() {
        return id;
    }

    //Devuelve el ID de quien publicó.
    public String getAutorId() {
        return autorId;
    }

    //Devuelve el texto de la publicación.
    public String getTexto() {
        return texto;
    }

    //Devuelve la fecha de la publicación en milisegundos.
    public long getCreadoEn() {
        return creadoEn;
    }

    //Devuelve si la publicación pertenece al muro, a una carrera o a una sede.
    public AmbitoPublicacion getAmbito() {
        return ambito;
    }

    //Devuelve si la publicación es general o una pregunta.
    public TipoPublicacion getTipo() {
        return tipo;
    }

    //Devuelve el ID de la carrera asociado a la publicación.
    public String getCarreraId() {
        return carreraId;
    }

    //Devuelve el ID de la sede asociado a la publicación.
    public String getSedeId() {
        return sedeId;
    }

    //Devuelve una copia de la lista de comentarios.
    public ArrayList<Comentario> getComentarios() {
        return new ArrayList<>(comentarios);
    }
}
