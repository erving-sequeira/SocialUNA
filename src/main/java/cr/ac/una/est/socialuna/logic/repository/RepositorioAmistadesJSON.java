package cr.ac.una.est.socialuna.logic.repository;

import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.socialuna.logic.model.entity.RelacionAmistad;
import cr.ac.una.est.socialuna.persistence.GestorJSON;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/** Relaciones de amistad en memoria con persistencia en amistades.json. */
public class RepositorioAmistadesJSON {
    private static final String ARCHIVO = "amistades.json";
    private static final Type TIPO = new TypeToken<ArrayList<RelacionAmistad>>() { }.getType();

    private final GestorJSON gestor;
    private ArrayList<RelacionAmistad> datos;

    public RepositorioAmistadesJSON(GestorJSON gestor) {
        if (gestor == null) {
            throw new IllegalArgumentException("El gestor JSON es obligatorio.");
        }
        this.gestor = gestor;
    }

    /** Carga una vez al iniciar; un archivo inválido propaga el error sin tocarlo. */
    public void cargar() {
        List<RelacionAmistad> leidos = gestor.leer(ARCHIVO, TIPO, new ArrayList<>());
        var relaciones = new ArrayList<RelacionAmistad>();
        var ids = new HashSet<String>();
        var pares = new HashSet<Set<String>>();
        try {
            for (RelacionAmistad guardada : leidos) {
                var relacion = new RelacionAmistad(guardada.getId(), guardada.getUsuarioAId(),
                        guardada.getUsuarioBId(), guardada.getCreadoEn());
                if (!ids.add(relacion.getId()) || !pares.add(Set.of(relacion.getUsuarioAId(), relacion.getUsuarioBId()))) {
                    throw new IllegalArgumentException("ID o par de amistad repetido.");
                }
                relaciones.add(relacion);
            }
        } catch (IllegalArgumentException | NullPointerException error) {
            throw new IllegalStateException("amistades.json contiene relaciones inválidas.", error);
        }
        datos = relaciones;
    }

    public void guardarEnArchivo() {
        exigirCarga();
        gestor.escribir(ARCHIVO, datos, TIPO);
    }

    public ArrayList<RelacionAmistad> listar() {
        exigirCarga();
        return new ArrayList<>(datos);
    }

    /** Devuelve la relación que une ese par sin importar el orden, o null si no existe. */
    public RelacionAmistad buscarEntre(String usuarioId, String otroId) {
        exigirCarga();
        if (usuarioId == null || otroId == null || usuarioId.strip().equals(otroId.strip())) return null;
        String primero = usuarioId.strip();
        String segundo = otroId.strip();
        for (RelacionAmistad relacion : datos) {
            if (relacion.involucra(primero) && relacion.involucra(segundo)) {
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
        datos.add(relacion);
        try {
            guardarEnArchivo();
        } catch (RuntimeException fallo) {
            datos.remove(relacion);
            throw fallo;
        }
    }

    public void eliminar(String id) {
        exigirCarga();
        int indice = buscarIndicePorId(id);
        if (indice < 0) {
            throw new IllegalArgumentException("No existe una relación con el ID " + id + ".");
        }
        RelacionAmistad retirada = datos.remove(indice);
        try {
            guardarEnArchivo();
        } catch (RuntimeException fallo) {
            datos.add(indice, retirada);
            throw fallo;
        }
    }

    private int buscarIndicePorId(String id) {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i).getId().equals(id)) {
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
