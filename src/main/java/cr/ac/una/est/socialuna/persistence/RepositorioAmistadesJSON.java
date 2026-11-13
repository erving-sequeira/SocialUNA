package cr.ac.una.est.socialuna.persistence;

import cr.ac.una.est.socialuna.model.entity.RelacionAmistad;
import java.util.ArrayList;

/** Relaciones de amistad en memoria con persistencia en amistades.json. */
public class RepositorioAmistadesJSON {
    private static final String ARCHIVO = "amistades.json";

    private GestorJSON gestor;
    private DocumentoAmistades datos;

    public RepositorioAmistadesJSON(GestorJSON gestor) {
        if (gestor == null) {
            throw new IllegalArgumentException("El gestor JSON es obligatorio.");
        }
        this.gestor = gestor;
    }

    /** Carga una vez al iniciar; un archivo inválido propaga el error sin tocarlo. */
    public void cargar() {
        datos = gestor.leer(ARCHIVO, DocumentoAmistades.class, new DocumentoAmistades());
    }

    public void guardarEnArchivo() {
        exigirCarga();
        gestor.escribir(ARCHIVO, datos, DocumentoAmistades.class);
    }

    public ArrayList<RelacionAmistad> listar() {
        exigirCarga();
        return new ArrayList<>(datos.getRelaciones());
    }

    /** Devuelve la relación que une ese par sin importar el orden, o null si no existe. */
    public RelacionAmistad buscarEntre(String usuarioId, String otroId) {
        exigirCarga();
        for (RelacionAmistad relacion : datos.getRelaciones()) {
            if (relacion.involucra(usuarioId) && relacion.involucra(otroId)) {
                return relacion;
            }
        }
        return null;
    }

    /** Rechaza relaciones repetidas por ID o por par no ordenado antes de guardar. */
    public void guardar(RelacionAmistad relacion) {
        exigirCarga();
        if (relacion == null) {
            throw new IllegalArgumentException("La relación es obligatoria.");
        }
        if (buscarIndicePorId(relacion.getId()) >= 0) {
            throw new IllegalArgumentException("Ya existe una relación con el ID " + relacion.getId() + ".");
        }
        if (buscarEntre(relacion.getUsuarioAId(), relacion.getUsuarioBId()) != null) {
            throw new IllegalArgumentException("Ya existe una amistad entre esos usuarios.");
        }
        datos.getRelaciones().add(relacion);
        try {
            guardarEnArchivo();
        } catch (RuntimeException fallo) {
            datos.getRelaciones().remove(relacion);
            throw fallo;
        }
    }

    public void eliminar(String id) {
        exigirCarga();
        int indice = buscarIndicePorId(id);
        if (indice < 0) {
            throw new IllegalArgumentException("No existe una relación con el ID " + id + ".");
        }
        RelacionAmistad retirada = datos.getRelaciones().remove(indice);
        try {
            guardarEnArchivo();
        } catch (RuntimeException fallo) {
            datos.getRelaciones().add(indice, retirada);
            throw fallo;
        }
    }

    private int buscarIndicePorId(String id) {
        for (int i = 0; i < datos.getRelaciones().size(); i++) {
            if (datos.getRelaciones().get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    private void exigirCarga() {
        if (datos == null) {
            throw new IllegalStateException("El repositorio de amistades no ha cargado el archivo.");
        }
    }
}
