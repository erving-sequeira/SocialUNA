package cr.ac.una.est.socialuna.model.entity;

import java.util.List;

public class Carrera {
    private String id;
    private String nombre;
    private boolean activa;
    private List<String> sedeIds;

    //Deja que Gson arme la carrera al leer el JSON.
    protected Carrera() { }

    //Crea una carrera con su ID, nombre y estado cuando todavía no tiene sedes asignadas.
    public Carrera(String id, String nombre, boolean activa) {
        this(id, nombre, activa, null);
    }

    //Crea una carrera con su ID, nombre, estado y los IDs de los campus donde se imparte.
    public Carrera(String id, String nombre, boolean activa, List<String> sedeIds) {
        this.id = requerido(id, "id");
        this.nombre = requerido(nombre, "nombre");
        this.activa = activa;
        this.sedeIds = sedeIds == null ? null
                : sedeIds.stream().map(sedeId -> requerido(sedeId, "El ID de la sede")).distinct().toList();
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

    //Devuelve los IDs de las sedes de la carrera. Si faltan, devuelve una lista vacía.
    public List<String> getSedeIds() {
        return sedeIds == null ? List.of() : List.copyOf(sedeIds);
    }

    //Devuelve true si el catálogo ya trae la lista de sedes, aunque esté vacía.
    public boolean tieneSedesConfiguradas() {
        return sedeIds != null;
    }

    //Devuelve true si la carrera está activa.
    public boolean isActiva() {
        return activa;
    }
}