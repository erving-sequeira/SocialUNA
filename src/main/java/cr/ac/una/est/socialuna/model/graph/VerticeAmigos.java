package cr.ac.una.est.socialuna.model.graph;

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

    /** Agrega sin duplicar; la autoamistad la rechaza GrafoAmigos. */
    public void agregarAdyacente(String destinoId) {
        if (!contieneAdyacente(destinoId)) {
            adyacentes.add(destinoId);
        }
    }

    /** Retira la adyacencia si existe; no es un error que falte. */
    public void eliminarAdyacente(String destinoId) {
        adyacentes.remove(destinoId);
    }

    public boolean contieneAdyacente(String destinoId) {
        return adyacentes.contains(destinoId);
    }

    public String getUsuarioId() { return usuarioId; }
    public ArrayList<String> getAdyacentes() { return adyacentes; }
}
