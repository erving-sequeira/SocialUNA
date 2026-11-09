package cr.ac.una.est.socialuna.model.entity;

/** Una arista no dirigida persistida por par de usuarios, sin autoamistad. */
public class RelacionAmistad {
    private String id;
    private String usuarioAId;
    private String usuarioBId;
    private long creadoEn;

    /** Reservado para la reconstrucción de datos persistidos. */
    protected RelacionAmistad() { }

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

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.strip();
    }

    /** null y los usuarios ajenos a esta amistad devuelven false. */
    public boolean involucra(String usuarioId) {
        return usuarioId != null && (usuarioId.equals(usuarioAId) || usuarioId.equals(usuarioBId));
    }

    /** Devuelve el extremo opuesto; un usuario ajeno es un argumento inválido. */
    public String obtenerOtroUsuario(String usuarioId) {
        if (!involucra(usuarioId)) {
            throw new IllegalArgumentException("El usuario no pertenece a esta amistad.");
        }
        return usuarioId.equals(usuarioAId) ? usuarioBId : usuarioAId;
    }

    public String getId() { return id; }
    public String getUsuarioAId() { return usuarioAId; }
    public String getUsuarioBId() { return usuarioBId; }
    public long getCreadoEn() { return creadoEn; }
}
