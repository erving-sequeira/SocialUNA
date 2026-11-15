package cr.ac.una.est.socialuna.persistence;

import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.persistence.GestorJSON;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class RepositorioSedesJSON {
    private static final String ARCHIVO = "sedes.json";
    private static final Type TIPO = new TypeToken<ArrayList<SedeCampus>>() { }.getType();
    private final GestorJSON gestor;
    private ArrayList<SedeCampus> sedes;

    //Recibe el gestor JSON y carga el catálogo de sedes.
    public RepositorioSedesJSON(GestorJSON gestor) {
        this.gestor = Objects.requireNonNull(gestor);
        cargar();
    }

    //Lee sedes.json y reemplaza el catálogo en memoria si los datos son válidos.
    public void cargar() {
        List<SedeCampus> leidas = gestor.leer(ARCHIVO, TIPO, new ArrayList<>());
        try {
            sedes = validarSedes(leidas);
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException(ARCHIVO + " contiene datos inválidos.", error);
        }
    }

    //Devuelve una copia de la lista de sedes.
    public ArrayList<SedeCampus> listarSedes() { return new ArrayList<>(sedes); }

    //Recibe un ID y devuelve la sede que coincide, o null si no está.
    public SedeCampus buscarSedePorId(String id) {
        for (SedeCampus sede : sedes) if (sede.getId().equals(id)) return sede;
        return null;
    }

    //Valida la lista recibida, la escribe en sedes.json y actualiza el catálogo en memoria.
    public void guardarSedes(List<SedeCampus> valores) {
        ArrayList<SedeCampus> actualizadas = validarSedes(valores);
        gestor.escribir(ARCHIVO, actualizadas, TIPO);
        sedes = actualizadas;
    }

    //Recibe una lista y devuelve una copia de sus sedes si todas son válidas y tienen IDs distintos.
    private static ArrayList<SedeCampus> validarSedes(List<SedeCampus> valores) {
        if (valores == null) throw new IllegalArgumentException("Falta la lista de sedes.");
        var resultado = new ArrayList<SedeCampus>();
        var ids = new HashSet<String>();
        for (SedeCampus valor : valores) {
            if (valor == null) throw new IllegalArgumentException("Hay una sede vacía.");
            var sede = new SedeCampus(valor.getId(), valor.getNombre(), valor.getAlias(), valor.getTipo(), valor.isActiva());
            if (!ids.add(sede.getId())) throw new IllegalArgumentException("ID de sede repetido.");
            resultado.add(sede);
        }
        return resultado;
    }
}
