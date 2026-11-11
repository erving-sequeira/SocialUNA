package cr.ac.una.est.socialuna.logic.repository;

import com.google.gson.reflect.TypeToken;
import cr.ac.una.est.socialuna.logic.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.persistence.GestorJSON;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/** Catálogo de sedes respaldado por sedes.json. */
public class RepositorioSedesJSON {
    private static final String ARCHIVO = "sedes.json";
    private static final Type TIPO = new TypeToken<ArrayList<SedeCampus>>() { }.getType();
    private final GestorJSON gestor;
    private ArrayList<SedeCampus> sedes;

    public RepositorioSedesJSON(GestorJSON gestor) {
        this.gestor = Objects.requireNonNull(gestor);
        cargar();
    }

    public void cargar() {
        List<SedeCampus> leidas = gestor.leer(ARCHIVO, TIPO, new ArrayList<>());
        try {
            sedes = validarSedes(leidas);
        } catch (IllegalArgumentException error) {
            throw new IllegalStateException(ARCHIVO + " contiene datos inválidos.", error);
        }
    }

    public ArrayList<SedeCampus> listarSedes() { return new ArrayList<>(sedes); }

    public SedeCampus buscarSedePorId(String id) {
        for (SedeCampus sede : sedes) if (sede.getId().equals(id)) return sede;
        return null;
    }

    public void guardarSedes(List<SedeCampus> valores) {
        ArrayList<SedeCampus> actualizadas = validarSedes(valores);
        gestor.escribir(ARCHIVO, actualizadas, TIPO);
        sedes = actualizadas;
    }

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
