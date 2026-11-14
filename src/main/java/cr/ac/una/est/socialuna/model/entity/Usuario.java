package cr.ac.una.est.socialuna.model.entity;

public class Usuario {
    private String id;
    private String nombre;
    private String nombreUsuario;
    private String passwordHash;
    private String carreraId;
    private String sedeId;

    //Deja que Gson arme el perfil al leer el JSON.
    protected Usuario() { }

    //Crea el perfil con ID, nombre, usuario, hash de la contraseña y los IDs de carrera y sede.
    public Usuario(String id, String nombre, String nombreUsuario, String passwordHash,
                   String carreraId, String sedeId) {
        this.id = requerido(id, "ID");
        this.nombre = requerido(nombre, "Nombre");
        this.nombreUsuario = requerido(nombreUsuario, "Nombre de usuario");
        this.passwordHash = requerido(passwordHash, "Hash de contraseña");
        setCarreraId(carreraId);
        setSedeId(sedeId);
    }

    //Recibe un valor y el nombre del campo. Devuelve el texto limpio o avisa si falta.
    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.strip();
    }

    //Recibe texto y lo devuelve sin espacios en los bordes, o null si venía vacío.
    private static String opcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.strip();
    }

    //Devuelve el ID con el que se guardó el perfil.
    public String getId() { return id; }
    //Devuelve el nombre que se muestra en el perfil.
    public String getNombre() { return nombre; }
    //Devuelve el nombre de usuario que se usa para entrar.
    public String getNombreUsuario() { return nombreUsuario; }
    //Devuelve el hash guardado de la contraseña.
    public String getPasswordHash() { return passwordHash; }
    //Devuelve el ID de la carrera, o null si no hay una seleccionada.
    public String getCarreraId() { return carreraId; }
    //Devuelve el ID de la sede, o null si no hay una seleccionada.
    public String getSedeId() { return sedeId; }

    //Cambia el ID de la carrera. Si llega vacío o null, quita la selección.
    public void setCarreraId(String carreraId) { this.carreraId = opcional(carreraId); }
    //Cambia el ID de la sede. Si llega vacío o null, quita la selección.
    public void setSedeId(String sedeId) { this.sedeId = opcional(sedeId); }
}
