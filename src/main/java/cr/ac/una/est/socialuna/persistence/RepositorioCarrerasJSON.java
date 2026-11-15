package cr.ac.una.est.socialuna.persistence;

import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.persistence.GestorJSON;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class RepositorioCarrerasJSON {
    private static final String ARCHIVO = "carreras.json";
    private static final Type TIPO = new TypeToken<ArrayList<Carrera>>() { }.getType();
    private final GestorJSON gestor;
    private ArrayList<Carrera> carreras;

    //Recibe el gestor JSON y carga el catálogo de carreras.
    public RepositorioCarrerasJSON(GestorJSON gestor) {
        this.gestor = Objects.requireNonNull(gestor);
        cargar();
    }

    //Lee carreras.json y reemplaza el catálogo en memoria si los datos son válidos.
    public void cargar() {
        List<Carrera> leidas = gestor.leer(ARCHIVO, TIPO, new ArrayList<>());
        try {
            carreras = validarCarreras(leidas);
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException(ARCHIVO + " contiene datos inválidos.", error);
        }
    }

    //Devuelve una copia de la lista de carreras.
    public ArrayList<Carrera> listarCarreras() { return new ArrayList<>(carreras); }

    //Recibe un ID y devuelve la carrera que coincide, o null si no está.
    public Carrera buscarCarreraPorId(String id) {
        for (Carrera carrera : carreras) if (carrera.getId().equals(id)) return carrera;
        return null;
    }

    //Valida la lista recibida, la escribe en carreras.json y actualiza el catálogo en memoria.
    public void guardarCarreras(List<Carrera> valores) {
        ArrayList<Carrera> actualizadas = validarCarreras(valores);
        gestor.escribir(ARCHIVO, actualizadas, TIPO);
        carreras = actualizadas;
    }

    //Recibe una lista y devuelve una copia de sus carreras si todas son válidas y tienen IDs distintos.
    private static ArrayList<Carrera> validarCarreras(List<Carrera> valores) {
        if (valores == null) throw new IllegalArgumentException("Falta la lista de carreras.");
        var resultado = new ArrayList<Carrera>();
        var ids = new HashSet<String>();
        for (Carrera valor : valores) {
            if (valor == null) throw new IllegalArgumentException("Hay una carrera vacía.");
            var carrera = new Carrera(valor.getId(), valor.getNombre(), valor.isActiva());
            if (!ids.add(carrera.getId())) throw new IllegalArgumentException("ID de carrera repetido.");
            resultado.add(carrera);
        }
        return resultado;
    }
}
