package cr.ac.una.est.socialuna.model.entity;

public class SedeCampus {
    private String id;
    private String nombre;
    private String alias;
    private String tipo;
    private boolean activa;

    public SedeCampus(String id, String nombre, String alias, String tipo, boolean activa) {
        this.id = id;
        this.nombre = nombre;
        this.alias = alias;
        this.tipo = tipo;
        this.activa = activa;
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