
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

    // Metodo para agregar comentarios
    public void agregarComentario(Comentario comentario) {
        if (comentario == null) {
            throw new IllegalArgumentException(
                    "El comentario es obligatorio.");
        }
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
