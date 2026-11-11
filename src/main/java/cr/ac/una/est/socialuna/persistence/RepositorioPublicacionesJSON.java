
package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.model.entity.Publicacion;
import java.util.ArrayList;

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
        datos = gestor.leer(
                ARCHIVO,
                DocumentoPublicaciones.class,
                new DocumentoPublicaciones()
        );
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

