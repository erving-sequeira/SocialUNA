package cr.ac.una.est.socialuna.logic.repository;

import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.socialuna.logic.model.entity.Carrera;
import cr.ac.una.est.socialuna.persistence.GestorJSON;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Catálogo de carreras respaldado por carreras.json. */
public class RepositorioCarrerasJSON {
    private static final String ARCHIVO = "carreras.json";
    private static final Type TIPO = new TypeToken<ArrayList<Carrera>>() { }.getType();
    private final GestorJSON gestor;
    private ArrayList<Carrera> carreras;

    public RepositorioCarrerasJSON(GestorJSON gestor) {
        this.gestor = Objects.requireNonNull(gestor);
        cargar();
    }

    public void cargar() {
        List<Carrera> leidas = gestor.leer(ARCHIVO, TIPO, new ArrayList<>());
        try {
            carreras = validarCarreras(leidas);
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException(ARCHIVO + " contiene datos inválidos.", error);
        }
    }

    public ArrayList<Carrera> listarCarreras() { return new ArrayList<>(carreras); }

    public Carrera buscarCarreraPorId(String id) {
        for (Carrera carrera : carreras) if (carrera.getId().equals(id)) return carrera;
        return null;
    }

    public void guardarCarreras(List<Carrera> valores) {
        ArrayList<Carrera> actualizadas = validarCarreras(valores);
        gestor.escribir(ARCHIVO, actualizadas, TIPO);
        carreras = actualizadas;
    }

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
