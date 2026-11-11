package cr.ac.una.est.socialuna.model.graph;

import java.util.ArrayList;

/** Grafo no dirigido de amistades por listas de adyacencia sobre IDs de usuario. */
public class GrafoAmigos {
    private ArrayList<VerticeAmigos> vertices = new ArrayList<>();

    public GrafoAmigos() { }

    /** Deja el grafo vacío; se usa antes de reconstruirlo desde el repositorio. */
    public void vaciar() {
        vertices.clear();
    }

    /** Idempotente: un usuario repetido no crea un vértice duplicado. */
    public void agregarUsuario(String usuarioId) {
        if (buscarVertice(usuarioId) == null) {
            vertices.add(new VerticeAmigos(usuarioId));
        }
    }

    /** Une en ambos sentidos; repetir la conexión no duplica adyacencias. */
    public void conectar(String usuarioAId, String usuarioBId) {
        exigirParDistinto(usuarioAId, usuarioBId);
        VerticeAmigos a = exigirVertice(usuarioAId);
        VerticeAmigos b = exigirVertice(usuarioBId);
        a.agregarAdyacente(b.getUsuarioId());
        b.agregarAdyacente(a.getUsuarioId());
    }

    /** Retira las dos adyacencias; si no hay amistad no hace nada. */
    public void desconectar(String usuarioAId, String usuarioBId) {
        exigirParDistinto(usuarioAId, usuarioBId);
        exigirVertice(usuarioAId).eliminarAdyacente(usuarioBId);
        exigirVertice(usuarioBId).eliminarAdyacente(usuarioAId);
    }

    /** Usuario desconocido o sin sesión no es amigo de nadie. */
    public boolean sonAmigos(String usuarioId, String otroId) {
        VerticeAmigos vertice = buscarVertice(usuarioId);
        return vertice != null && vertice.contieneAdyacente(otroId);
    }

    /** Lista copia para que el llamador no altere las adyacencias del vértice. */
    public ArrayList<String> obtenerAmigos(String usuarioId) {
        VerticeAmigos vertice = buscarVertice(usuarioId);
        return vertice == null ? new ArrayList<>() : new ArrayList<>(vertice.getAdyacentes());
    }

    private VerticeAmigos buscarVertice(String usuarioId) {
        if (usuarioId == null) {
            return null;
        }
        for (VerticeAmigos vertice : vertices) {
            if (vertice.getUsuarioId().equals(usuarioId.strip())) {
                return vertice;
            }
        }
        return null;
    }

    private VerticeAmigos exigirVertice(String usuarioId) {
        VerticeAmigos vertice = buscarVertice(usuarioId);
        if (vertice == null) {
            throw new IllegalArgumentException("El usuario " + usuarioId + " no está registrado en el grafo.");
        }
        return vertice;
    }

    private static void exigirParDistinto(String usuarioAId, String usuarioBId) {
        if (usuarioAId == null || usuarioAId.isBlank() || usuarioBId == null || usuarioBId.isBlank()) {
            throw new IllegalArgumentException("Se requieren dos usuarios para la operación de amistad.");
        }
        if (usuarioAId.strip().equals(usuarioBId.strip())) {
            throw new IllegalArgumentException("Un usuario no puede ser amigo de sí mismo.");
        }
    }
}
