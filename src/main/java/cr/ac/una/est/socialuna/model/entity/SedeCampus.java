package cr.ac.una.est.socialuna.model.entity;

public class SedeCampus {
    private String id;
    private String nombre;
    private String alias;
    private String tipo;
    private boolean activa;

    //Deja que Gson arme la sede al leer el JSON.
    protected SedeCampus() { }

    //Crea una sede con ID, nombre, alias, tipo y estado. Revisa que haya ID y nombre.
    public SedeCampus(String id, String nombre, String alias, String tipo, boolean activa) {
        this.id = requerido(id, "id");
        this.nombre = requerido(nombre, "nombre");
        this.alias = alias;
        this.tipo = tipo;
        this.activa = activa;
    }

    //Recibe un valor y el nombre del campo. Devuelve el texto limpio o avisa si falta.
    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.strip();
    }

    //Devuelve el ID de la sede.
    public String getId() {
        return id;
    }

    //Devuelve el nombre de la sede.
    public String getNombre() {
        return nombre;
    }

    //Devuelve el alias de la sede, si tiene uno.
    public String getAlias() {
        return alias;
    }

    //Devuelve el tipo registrado para la sede.
    public String getTipo() {
        return tipo;
    }

    //Devuelve true si la sede está activa.
    public boolean isActiva() {
        return activa;
    }
}