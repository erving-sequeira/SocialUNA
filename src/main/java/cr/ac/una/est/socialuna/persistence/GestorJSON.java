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

/** Lectura y escritura de JSON con Gson, sin adaptador propio de listas. */
public class GestorJSON {
    private final Path directorioDatos;
    private final Gson gson;

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
                    // El error de limpieza no debe ocultar el resultado del guardado.
                }
            }
        }
    }

    private Gson configurarGson() {
        return new GsonBuilder().setStrictness(Strictness.STRICT).setPrettyPrinting().create();
    }

    private Path resolverRuta(String nombreArchivo) {
        if (nombreArchivo == null || nombreArchivo.isBlank()) {
            throw new IllegalArgumentException("El nombre del archivo es obligatorio.");
        }
        return directorioDatos.resolve(nombreArchivo);
    }
}
