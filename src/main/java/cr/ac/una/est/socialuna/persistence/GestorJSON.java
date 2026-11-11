package cr.ac.una.est.socialuna.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

/** Lectura y escritura de JSON con Gson, sin adaptador propio de listas. */
public class GestorJSON {
    private Path directorioDatos;
    private Gson gson;

    public GestorJSON(Path directorioDatos) {
        if (directorioDatos == null) {
            throw new IllegalArgumentException("El directorio de datos es obligatorio.");
        }
        this.directorioDatos = directorioDatos;
        this.gson = configurarGson();
    }

    /**
     * Archivo ausente o en blanco devuelve inicial. JSON inválido lanza
     * IllegalStateException sin modificar el archivo.
     */
    public <T> T leer(String nombreArchivo, Type tipo, T inicial) {
        Path ruta = resolverRuta(nombreArchivo);
        if (!Files.exists(ruta)) {
            return inicial;
        }
        String contenido;
        try {
            contenido = Files.readString(ruta);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer " + nombreArchivo + ".", e);
        }
        if (contenido.isBlank()) {
            return inicial;
        }
        try {
            T dato = gson.fromJson(contenido, tipo);
            return dato == null ? inicial : dato;
        } catch (JsonParseException | IllegalStateException e) {
            throw new IllegalStateException(
                    nombreArchivo + " contiene JSON inválido; se conserva su contenido sin modificarlo.", e);
        }
    }

    /** Un fallo de escritura nunca se presenta como guardado exitoso. */
    public <T> void escribir(String nombreArchivo, T documento, Type tipo) {
        try {
            Files.createDirectories(directorioDatos);
            Files.writeString(resolverRuta(nombreArchivo), gson.toJson(documento, tipo));
        } catch (IOException | UnsupportedOperationException e) {
            throw new IllegalStateException("No se pudo guardar " + nombreArchivo + ".", e);
        }
    }

    private Gson configurarGson() {
        return new GsonBuilder().setPrettyPrinting().create();
    }

    private Path resolverRuta(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo es obligatorio.");
        }
        return directorioDatos.resolve(nombreArchivo);
    }
}
