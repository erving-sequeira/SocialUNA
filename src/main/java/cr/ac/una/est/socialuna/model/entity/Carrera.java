package cr.ac.una.est.socialuna.model.entity;

public class Carrera {
    private String id;
    private String nombre;
    private boolean activa;

    public Carrera(String id, String nombre, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.activa = activa;
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