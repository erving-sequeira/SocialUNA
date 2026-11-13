package cr.ac.una.est.socialuna.model.entity;

public class SedeCampus {
    private String id;
    private String nombre;
    private String alias;
    private String tipo;
    private boolean activa;

    /** Reservado para reconstrucción de datos persistidos. */
    protected SedeCampus() { }

    public SedeCampus(String id, String nombre, String alias, String tipo, boolean activa) {
        this.id = requerido(id, "id");
        this.nombre = requerido(nombre, "nombre");
        this.alias = alias;
        this.tipo = tipo;
        this.activa = activa;
    }

    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.strip();
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getAlias() {
        return alias;
    }

    public String getTipo() {
        return tipo;
    }

    public boolean isActiva() {
        return activa;
    }
}