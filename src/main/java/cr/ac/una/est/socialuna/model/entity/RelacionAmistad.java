package cr.ac.una.est.socialuna.model.entity;

public class RelacionAmistad {
    private String id;
    private String usuarioAId;
    private String usuarioBId;
    private long creadoEn;

    //Deja que Gson arme la amistad al leer el JSON.
    protected RelacionAmistad() { }

    //Crea una amistad con su ID, los IDs de dos usuarios distintos y la fecha.
    public RelacionAmistad(String id, String usuarioAId, String usuarioBId, long creadoEn) {
        this.id = requerido(id, "ID");
        this.usuarioAId = requerido(usuarioAId, "Primer usuario");
        this.usuarioBId = requerido(usuarioBId, "Segundo usuario");
        if (this.usuarioAId.equals(this.usuarioBId)) {
            throw new IllegalArgumentException("Un usuario no puede ser amigo de sí mismo.");
        }
        if (creadoEn < 0) {
            throw new IllegalArgumentException("La fecha de creación no puede ser negativa.");
        }
        this.creadoEn = creadoEn;
    }

    //Recibe un valor y el nombre del campo. Devuelve el texto limpio o avisa si falta.
    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.strip();
    }

    //Recibe un ID y devuelve true si es de alguno de los dos amigos.
    public boolean involucra(String usuarioId) {
        return usuarioId != null && (usuarioId.equals(usuarioAId) || usuarioId.equals(usuarioBId));
    }

    //Dado el ID de uno de los amigos, devuelve el del otro. Si el ID es ajeno, da error.
    public String obtenerOtroUsuario(String usuarioId) {
        if (!involucra(usuarioId)) {
            throw new IllegalArgumentException("El usuario no pertenece a esta amistad.");
        }
        return usuarioId.equals(usuarioAId) ? usuarioBId : usuarioAId;
    }

    //Devuelve el ID de la amistad.
    public String getId() { return id; }
    //Devuelve el ID del primer usuario de la amistad.
    public String getUsuarioAId() { return usuarioAId; }
    //Devuelve el ID del segundo usuario de la amistad.
    public String getUsuarioBId() { return usuarioBId; }
    //Devuelve la fecha de la amistad en milisegundos.
    public long getCreadoEn() { return creadoEn; }
}
