package cr.ac.una.est.socialuna.model.graph;

import java.util.ArrayList;

public class VerticeAmigos {
    private String usuarioId;
    private ArrayList<String> adyacentes = new ArrayList<>();

    //Recibe el ID del usuario y crea su vértice sin amigos.
    public VerticeAmigos(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio.");
        }
        this.usuarioId = usuarioId.strip();
    }

    //Agrega el ID de otro usuario como vecino si todavía no está en la lista.
    public void agregarAdyacente(String destinoId) {
        if (destinoId == null || destinoId.isBlank() || usuarioId.equals(destinoId.strip())) {
            throw new IllegalArgumentException("El ID del vecino debe pertenecer a otro usuario.");
        }
        if (!contieneAdyacente(destinoId)) {
            adyacentes.add(destinoId.strip());
        }
    }

    //Recibe el ID de un vecino y lo quita de la lista si está.
    public void eliminarAdyacente(String destinoId) {
        if (destinoId != null) adyacentes.remove(destinoId.strip());
    }

    //Recibe un ID y devuelve true si está en la lista de vecinos.
    public boolean contieneAdyacente(String destinoId) {
        return destinoId != null && adyacentes.contains(destinoId.strip());
    }

    //Devuelve el ID del usuario al que pertenece el vértice.
    public String getUsuarioId() { return usuarioId; }
    //Devuelve una copia de los IDs de los vecinos.
    public ArrayList<String> getAdyacentes() { return new ArrayList<>(adyacentes); }
}
