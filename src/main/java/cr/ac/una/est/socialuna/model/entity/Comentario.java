package cr.ac.una.est.socialuna.model.entity;

public class Comentario {
    private String id;
    private String autorId;
    private String texto;
    private long creadoEn;

    //Deja que Gson arme el comentario al leer el JSON.
    protected Comentario() { }

    //Crea un comentario con su ID, autor, texto y fecha. Revisa que los datos vengan completos.
    public Comentario(String id, String autorId, String texto, long creadoEn) {
        if (id == null || id.isBlank() || autorId == null || autorId.isBlank()
                || texto == null || texto.isBlank() || creadoEn < 0) {
            throw new IllegalArgumentException("El comentario requiere ID, autor, texto y fecha válidos.");
        }
        this.id = id.strip();
        this.autorId = autorId.strip();
        this.texto = texto.strip();
        this.creadoEn = creadoEn;
    }

    //Devuelve el ID del comentario.
    public String getId() { return id; }
    //Devuelve el ID de quien escribió el comentario.
    public String getAutorId() { return autorId; }
    //Devuelve el texto del comentario.
    public String getTexto() { return texto; }
    //Devuelve la fecha del comentario en milisegundos.
    public long getCreadoEn() { return creadoEn; }
}
