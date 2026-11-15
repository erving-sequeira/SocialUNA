package cr.ac.una.est.socialuna.model.graph;

import java.util.ArrayList;

public class GrafoAmigos {
    private ArrayList<VerticeAmigos> vertices = new ArrayList<>();

    //Crea un grafo que empieza sin usuarios ni amistades.
    public GrafoAmigos() { }

    //Borra todos los usuarios y conexiones del grafo.
    public void vaciar() {
        vertices.clear();
    }

    //Recibe el ID de un usuario y lo agrega al grafo si todavía no está.
    public void agregarUsuario(String usuarioId) {
        if (buscarVertice(usuarioId) == null) {
            vertices.add(new VerticeAmigos(usuarioId));
        }
    }

    //Recibe dos IDs y conecta a los usuarios en ambos sentidos sin duplicar la amistad.
    public void conectar(String usuarioAId, String usuarioBId) {
        exigirParDistinto(usuarioAId, usuarioBId);
        VerticeAmigos a = exigirVertice(usuarioAId);
        VerticeAmigos b = exigirVertice(usuarioBId);
        a.agregarAdyacente(b.getUsuarioId());
        b.agregarAdyacente(a.getUsuarioId());
    }

    //Recibe dos IDs y quita la conexión en ambos sentidos si existe.
    public void desconectar(String usuarioAId, String usuarioBId) {
        exigirParDistinto(usuarioAId, usuarioBId);
        VerticeAmigos a = exigirVertice(usuarioAId);
        VerticeAmigos b = exigirVertice(usuarioBId);
        a.eliminarAdyacente(b.getUsuarioId());
        b.eliminarAdyacente(a.getUsuarioId());
    }

    //Dado un par de IDs, devuelve true si los usuarios están conectados.
    public boolean sonAmigos(String usuarioId, String otroId) {
        VerticeAmigos vertice = buscarVertice(usuarioId);
        return vertice != null && vertice.contieneAdyacente(otroId);
    }

    //Recibe un ID y devuelve una copia de los IDs de sus amigos, o una lista vacía si no está.
    public ArrayList<String> obtenerAmigos(String usuarioId) {
        VerticeAmigos vertice = buscarVertice(usuarioId);
        return vertice == null ? new ArrayList<>() : vertice.getAdyacentes();
    }

    //Busca el ID y devuelve su vértice, o null si no está en el grafo.
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

    //Devuelve el vértice del ID recibido. Da error si ese usuario no está en el grafo.
    private VerticeAmigos exigirVertice(String usuarioId) {
        VerticeAmigos vertice = buscarVertice(usuarioId);
        if (vertice == null) {
            throw new IllegalArgumentException("El usuario " + usuarioId + " no está registrado en el grafo.");
        }
        return vertice;
    }

    //Revisa que los dos IDs tengan texto y pertenezcan a usuarios distintos.
    private static void exigirParDistinto(String usuarioAId, String usuarioBId) {
        if (usuarioAId == null || usuarioAId.isBlank() || usuarioBId == null || usuarioBId.isBlank()) {
            throw new IllegalArgumentException("Se requieren dos usuarios para la operación de amistad.");
        }
        if (usuarioAId.strip().equals(usuarioBId.strip())) {
            throw new IllegalArgumentException("Un usuario no puede ser amigo de sí mismo.");
        }
    }
}
