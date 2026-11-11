package cr.ac.una.est.socialuna.logic.model.entity;

public class Carrera {
    private String id;
    private String nombre;
    private boolean activa;

    /** Reservado para reconstrucción de datos persistidos. */
    protected Carrera() { }

    public Carrera(String id, String nombre, boolean activa) {
        this.id = requerido(id, "id");
        this.nombre = requerido(nombre, "nombre");
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

    public boolean isActiva() {
        return activa;
    }
}