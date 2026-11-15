
package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.model.entity.Comentario;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import java.util.ArrayList;
import java.util.HashSet;

public class RepositorioPublicacionesJSON {

    private static final String ARCHIVO = "publicaciones.json";

    private GestorJSON gestor;
    private DocumentoPublicaciones datos;

    //Recibe el gestor JSON para poder cargar y guardar las publicaciones.
    public RepositorioPublicacionesJSON(GestorJSON gestor) {
        if (gestor == null) {
            throw new IllegalArgumentException(
                    "El gestor JSON es obligatorio.");
        }
        this.gestor = gestor;
    }

    //Lee publicaciones.json y reconstruye la lista con sus comentarios después de validarlos.
    public void cargar() {
        DocumentoPublicaciones leido = gestor.leer(
                ARCHIVO,
                DocumentoPublicaciones.class,
                new DocumentoPublicaciones()
        );

        var publicaciones = new ArrayList<Publicacion>();
        var ids = new HashSet<String>();

        try {
            for (Publicacion registro : leido.getPublicaciones()) {

                Publicacion publicacion = new Publicacion(
                        registro.getId(),
                        registro.getAutorId(),
                        registro.getTexto(),
                        registro.getCreadoEn(),
                        registro.getAmbito(),
                        registro.getTipo(),
                        registro.getCarreraId(),
                        registro.getSedeId()
                );

                if (!ids.add(publicacion.getId())) {
                    throw new IllegalArgumentException(
                            "ID de publicación repetido.");
                }

                for (Comentario comentario : registro.getComentarios()) {
                    publicacion.agregarComentario(new Comentario(
                            comentario.getId(),
                            comentario.getAutorId(),
                            comentario.getTexto(),
                            comentario.getCreadoEn()
                    ));
                }

                publicaciones.add(publicacion);
            }
        } catch (IllegalArgumentException | NullPointerException error) {
            throw new IllegalStateException(
                    ARCHIVO + " contiene publicaciones inválidas.", error);
        }

        datos = new DocumentoPublicaciones(publicaciones);
    }

    //Guarda en publicaciones.json el documento que está en memoria.
    public void guardarEnArchivo() {
        exigirCarga();
        gestor.escribir(
                ARCHIVO,
                datos,
                DocumentoPublicaciones.class
        );
    }

    //Devuelve una copia de la lista de publicaciones cargada.
    public ArrayList<Publicacion> listar() {
        exigirCarga();
        return new ArrayList<>(datos.getPublicaciones());
    }

    //Recibe un ID y devuelve la publicación que coincide, o null si no está.
    public Publicacion buscarPorId(String id) {
        exigirCarga();

        int indice = buscarIndicePorId(id);

        if (indice < 0) {
            return null;
        }

        return datos.getPublicaciones().get(indice);
    }

    //Recibe una publicación nueva, revisa su ID y la guarda.
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

    //Recibe el ID de una publicación y el comentario que se agrega y guarda.
    public void agregarComentario(String publicacionId, Comentario comentario) {
        exigirCarga();

        int indice = buscarIndicePorId(publicacionId);

        if (indice < 0) {
            throw new IllegalArgumentException(
                    "No existe la publicación indicada.");
        }

        Publicacion anterior = datos.getPublicaciones().get(indice);

        Publicacion actualizada = new Publicacion(
                anterior.getId(),
                anterior.getAutorId(),
                anterior.getTexto(),
                anterior.getCreadoEn(),
                anterior.getAmbito(),
                anterior.getTipo(),
                anterior.getCarreraId(),
                anterior.getSedeId()
        );

        for (Comentario guardado : anterior.getComentarios()) {
            actualizada.agregarComentario(guardado);
        }
        actualizada.agregarComentario(comentario);

        datos.getPublicaciones().set(indice, actualizada);

        try {
            guardarEnArchivo();
        } catch (RuntimeException fallo) {
            datos.getPublicaciones().set(indice, anterior);
            throw fallo;
        }
    }

    //Busca el ID de la publicación y devuelve su posición en la lista, o -1 si no está.
    private int buscarIndicePorId(String id) {
        for (int i = 0; i < datos.getPublicaciones().size(); i++) {
            if (datos.getPublicaciones().get(i).getId().equals(id)) {
                return i;
            }
        }

        return -1;
    }

    //Da error si se intenta usar el repositorio antes de cargarlo.
    private void exigirCarga() {
        if (datos == null) {
            throw new IllegalStateException(
                    "El repositorio de publicaciones "
                            + "no ha cargado el archivo.");
        }
    }
}

