package cr.ac.una.est.socialuna.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.Strictness;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class GestorJSON {
    private final Path directorioDatos;
    private final Gson gson;

    //Recibe la carpeta donde se guardan los datos y prepara Gson.
    public GestorJSON(Path directorioDatos) {
        if (directorioDatos == null) {
            throw new IllegalArgumentException("El directorio de datos es obligatorio.");
        }
        this.directorioDatos = directorioDatos;
        this.gson = configurarGson();
    }

    //Lee el archivo con el tipo indicado y devuelve su contenido, o el valor inicial si falta o está vacío.
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

    //Recibe el nombre del archivo, el documento y su tipo. Guarda el JSON pasando por un archivo temporal.
    public <T> void escribir(String nombreArchivo, T documento, Type tipo) {
        Path destino = resolverRuta(nombreArchivo);
        Path temporal = null;
        try {
            String contenido = gson.toJson(documento, tipo);
            Files.createDirectories(directorioDatos);
            temporal = Files.createTempFile(directorioDatos, ".socialuna-", ".tmp");
            Files.writeString(temporal, contenido);
            try {
                Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException error) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException | UnsupportedOperationException error) {
            throw new IllegalStateException("No se pudo guardar " + nombreArchivo + ".", error);
        } finally {
            if (temporal != null) {
                try {
                    Files.deleteIfExists(temporal);
                } catch (IOException ignorado) {
                }
            }
        }
    }

    //Devuelve Gson configurado para leer JSON estricto y escribirlo con sangría.
    private Gson configurarGson() {
        return new GsonBuilder().setStrictness(Strictness.STRICT).setPrettyPrinting().create();
    }

    //Recibe el nombre del archivo y devuelve su ruta dentro de la carpeta de datos.
    private Path resolverRuta(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo es obligatorio.");
        }
        return directorioDatos.resolve(nombreArchivo);
    }
}
