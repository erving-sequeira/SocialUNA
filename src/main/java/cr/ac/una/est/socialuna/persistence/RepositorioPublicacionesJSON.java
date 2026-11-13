
package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.logic.model.entity.Publicacion;
import cr.ac.una.est.socialuna.logic.model.entity.Comentario;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Repositorio de publicaciones con persistencia
 * en el archivo publicaciones.json.
 */
public class RepositorioPublicacionesJSON {

    private static final String ARCHIVO = "publicaciones.json";

    private GestorJSON gestor;
    private DocumentoPublicaciones datos;

    // Constructor
    public RepositorioPublicacionesJSON(GestorJSON gestor) {
        if (gestor == null) {
            throw new IllegalArgumentException(
                    "El gestor JSON es obligatorio.");
        }
        this.gestor = gestor;
    }

    // Cargar publicaciones desde el archivo
    public void cargar() {
        DocumentoPublicaciones leido = gestor.leer(ARCHIVO, DocumentoPublicaciones.class, new DocumentoPublicaciones());
        var publicaciones = new ArrayList<Publicacion>();
        var ids = new HashSet<String>();
        try {
            for (Publicacion registro : leido.getPublicaciones()) {
                Publicacion publicacion = new Publicacion(registro.getId(), registro.getAutorId(),
                        registro.getTexto(), registro.getCreadoEn(), registro.getAmbito(), registro.getTipo(),
                        registro.getCarreraId(), registro.getSedeId());
                if (!ids.add(publicacion.getId())) {
                    throw new IllegalArgumentException("ID de publicación repetido.");
                }
                for (Comentario comentario : registro.getComentarios()) {
                    publicacion.agregarComentario(new Comentario(comentario.getId(), comentario.getAutorId(),
                            comentario.getTexto(), comentario.getCreadoEn()));
                }
                publicaciones.add(publicacion);
            }
        } catch (IllegalArgumentException | NullPointerException error) {
            throw new IllegalStateException(ARCHIVO + " contiene publicaciones inválidas.", error);
        }
        datos = new DocumentoPublicaciones(publicaciones);
    }

    // Guardar publicaciones en el archivo
    public void guardarEnArchivo() {
        exigirCarga();
        gestor.escribir(
                ARCHIVO,
                datos,
                DocumentoPublicaciones.class
        );
    }

    // Obtener todas las publicaciones
    public ArrayList<Publicacion> listar() {
        exigirCarga();
        return new ArrayList<>(datos.getPublicaciones());
    }

    // Buscar una publicación por su ID
    public Publicacion buscarPorId(String id) {
        exigirCarga();

        int indice = buscarIndicePorId(id);

        if (indice < 0) {
            return null;
        }

        return datos.getPublicaciones().get(indice);
    }

    // Guardar una nueva publicación
    public void guardar(Publicacion publicacion) {
        exigirCarga();

        if (publicacion == null) {
            throw new IllegalArgumentException(
                    "La publicación es obligatoria.");
        }

        if (buscarIndicePorId(publicacion.getId()) >= 0) {
            throw new IllegalArgumentException(
                    "Ya existe una publicación con el ID "
                            + publicacion.getId() + ".");
        }

        datos.getPublicaciones().add(publicacion);

        try {
            guardarEnArchivo();
        } catch (RuntimeException fallo) {
            datos.getPublicaciones().remove(publicacion);
            throw fallo;
        }
    }

    /** Persiste el comentario antes de actualizar la publicación en memoria. */
    public void agregarComentario(String publicacionId, Comentario comentario) {
        exigirCarga();
        int indice = buscarIndicePorId(publicacionId);
        if (indice < 0) throw new IllegalArgumentException("No existe la publicación indicada.");
        var publicaciones = listar();
        Publicacion anterior = publicaciones.get(indice);
        var actualizada = new Publicacion(anterior.getId(), anterior.getAutorId(), anterior.getTexto(),
                anterior.getCreadoEn(), anterior.getAmbito(), anterior.getTipo(), anterior.getCarreraId(), anterior.getSedeId());
        for (Comentario guardado : anterior.getComentarios()) {
            actualizada.agregarComentario(guardado);
        }
        actualizada.agregarComentario(comentario);
        publicaciones.set(indice, actualizada);
        var actualizado = new DocumentoPublicaciones(publicaciones);
        gestor.escribir(ARCHIVO, actualizado, DocumentoPublicaciones.class);
        datos = actualizado;
    }

    // Buscar la posición de una publicación por ID
    private int buscarIndicePorId(String id) {
        for (int i = 0; i < datos.getPublicaciones().size(); i++) {
            if (datos.getPublicaciones().get(i).getId().equals(id)) {
                return i;
            }
        }

        return -1;
    }

    // Comprobar que el repositorio haya cargado los datos
    private void exigirCarga() {
        if (datos == null) {
            throw new IllegalStateException(
                    "El repositorio de publicaciones "
                            + "no ha cargado el archivo.");
        }
    }
}

