package cr.ac.una.est.socialuna.logic.model.entity;

/** Comentario de texto de un solo nivel. */
public class Comentario {
    private String id;
    private String autorId;
    private String texto;
    private long creadoEn;

    protected Comentario() { }

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

    public String getId() { return id; }
    public String getAutorId() { return autorId; }
    public String getTexto() { return texto; }
    public long getCreadoEn() { return creadoEn; }
}
