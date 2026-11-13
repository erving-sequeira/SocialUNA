package cr.ac.una.est.socialuna.model.entity;

/** Identidad del estudiante. Solo los datos académicos se pueden cambiar. */
public class Usuario {
    private String id;
    private String nombre;
    private String nombreUsuario;
    private String passwordHash;
    private String carreraId;
    private String sedeId;

    /** Reservado para la reconstrucción de datos persistidos. */
    protected Usuario() { }

    public Usuario(String id, String nombre, String nombreUsuario, String passwordHash,
                   String carreraId, String sedeId) {
        this.id = requerido(id, "ID");
        this.nombre = requerido(nombre, "Nombre");
        this.nombreUsuario = requerido(nombreUsuario, "Nombre de usuario");
        this.passwordHash = requerido(passwordHash, "Hash de contraseña");
        setCarreraId(carreraId);
        setSedeId(sedeId);
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.strip();
    }

    private static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getNombreUsuario() { return nombreUsuario; }
    public String getPasswordHash() { return passwordHash; }
    public String getCarreraId() { return carreraId; }
    public String getSedeId() { return sedeId; }

    /** null o texto vacío elimina la selección. El servicio valida el catálogo. */
    public void setCarreraId(String carreraId) { this.carreraId = opcional(carreraId); }
    /** null o texto vacío elimina la selección. El servicio valida el catálogo. */
    public void setSedeId(String sedeId) { this.sedeId = opcional(sedeId); }
}
