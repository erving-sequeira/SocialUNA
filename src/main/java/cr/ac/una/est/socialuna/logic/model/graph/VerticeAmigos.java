package cr.ac.una.est.socialuna.logic.model.graph;

import java.util.ArrayList;

/** Vértice del grafo de amistades: un usuario y sus IDs de vecinos, sin duplicados. */
public class VerticeAmigos {
    private String usuarioId;
    private ArrayList<String> adyacentes = new ArrayList<>();

    public VerticeAmigos(String usuarioId) {
        if (usuarioId == null || usuarioId.isBlank()) {
            throw new IllegalArgumentException("El ID del usuario es obligatorio.");
        }
        this.usuarioId = usuarioId.strip();
    }

    /** Agrega un vecino válido sin duplicar ni crear autoamistades. */
    public void agregarAdyacente(String destinoId) {
        if (destinoId == null || destinoId.isBlank() || usuarioId.equals(destinoId.strip())) {
            throw new IllegalArgumentException("El ID del vecino debe pertenecer a otro usuario.");
        }
        if (!contieneAdyacente(destinoId)) {
            adyacentes.add(destinoId.strip());
        }
    }

    /** Retira la adyacencia si existe; no es un error que falte. */
    public void eliminarAdyacente(String destinoId) {
        if (destinoId != null) adyacentes.remove(destinoId.strip());
    }

    public boolean contieneAdyacente(String destinoId) {
        return destinoId != null && adyacentes.contains(destinoId.strip());
    }

    public String getUsuarioId() { return usuarioId; }
    public ArrayList<String> getAdyacentes() { return new ArrayList<>(adyacentes); }
}
