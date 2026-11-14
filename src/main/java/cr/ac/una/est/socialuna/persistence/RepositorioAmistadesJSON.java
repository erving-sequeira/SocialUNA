package cr.ac.una.est.socialuna.persistence;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.socialuna.model.entity.RelacionAmistad;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class RepositorioAmistadesJSON {
    private static final String ARCHIVO = "amistades.json";
    private static final Type TIPO = new TypeToken<ArrayList<RelacionAmistad>>() { }.getType();

    private final GestorJSON gestor;
    private ArrayList<RelacionAmistad> datos;

    //Recibe el gestor JSON para poder cargar y guardar las amistades.
    public RepositorioAmistadesJSON(GestorJSON gestor) {
        if (gestor == null) {
            throw new IllegalArgumentException("El gestor JSON es obligatorio.");
        }
        this.gestor = gestor;
    }

    //Lee amistades.json con o sin la clave relaciones, y deja en memoria los pares válidos sin repetir.
    public void cargar() {
        JsonElement contenido = gestor.leer(ARCHIVO, JsonElement.class, new JsonArray());
        if (contenido.isJsonObject()) {
            contenido = contenido.getAsJsonObject().get("relaciones");
        }
        if (contenido == null || !contenido.isJsonArray()) {
            throw new IllegalStateException("amistades.json debe contener un arreglo de relaciones.");
        }
        var relaciones = new ArrayList<RelacionAmistad>();
        var ids = new HashSet<String>();
        var pares = new HashSet<Set<String>>();
        try {
            List<RelacionAmistad> leidos = new Gson().fromJson(contenido, TIPO);
            for (RelacionAmistad guardada : leidos) {
                var relacion = new RelacionAmistad(guardada.getId(), guardada.getUsuarioAId(),
                        guardada.getUsuarioBId(), guardada.getCreadoEn());
                if (!ids.add(relacion.getId()) || !pares.add(Set.of(relacion.getUsuarioAId(), relacion.getUsuarioBId()))) {
                    throw new IllegalArgumentException("ID o par de amistad repetido.");
                }
                relaciones.add(relacion);
            }
        } catch (JsonParseException | IllegalArgumentException | NullPointerException error) {
            throw new IllegalStateException("amistades.json contiene relaciones inválidas.", error);
        }
        datos = relaciones;
    }

    //Escribe la lista de amistades en amistades.json.
    public void guardarEnArchivo() {
        exigirCarga();
        gestor.escribir(ARCHIVO, datos, TIPO);
    }

    //Devuelve una copia de la lista de amistades cargada.
    public ArrayList<RelacionAmistad> listar() {
        exigirCarga();
        return new ArrayList<>(datos);
    }

    //Recibe dos IDs y devuelve su amistad sin importar el orden, o null si no existe.
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

    //Guarda la amistad recibida si no se repite ni el ID ni el par de usuarios.
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

    //Recibe el ID de una amistad y la borra del archivo y de la lista en memoria.
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

    //Busca el ID de amistad y devuelve su posición en la lista, o -1 si no está.
    private int buscarIndicePorId(String id) {
        for (int i = 0; i < datos.size(); i++) {
            if (datos.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    //Da error si se intenta usar el repositorio antes de cargarlo.
    private void exigirCarga() {
        if (datos == null) {
            throw new IllegalStateException("El repositorio de amistades no ha cargado el archivo.");
        }
    }
}
