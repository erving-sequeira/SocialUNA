package cr.ac.una.est.socialuna.model.entity;

public class Carrera {
    private String id;
    private String nombre;
    private boolean activa;

    //Deja que Gson arme la carrera al leer el JSON.
    protected Carrera() { }

    //Crea una carrera con su ID, nombre y estado. El ID y el nombre son obligatorios.
    public Carrera(String id, String nombre, boolean activa) {
        this.id = requerido(id, "id");
        this.nombre = requerido(nombre, "nombre");
        this.activa = activa;
    }

    //Recibe un valor y el nombre del campo. Devuelve el texto sin espacios en los bordes o avisa si falta.
    private static String requerido(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        return valor.strip();
    }

    //Devuelve el ID de la carrera.
    public String getId() {
        return id;
    }

    //Devuelve el nombre de la carrera.
    public String getNombre() {
        return nombre;
    }

    //Devuelve true si la carrera está activa.
    public boolean isActiva() {
        return activa;
    }
}