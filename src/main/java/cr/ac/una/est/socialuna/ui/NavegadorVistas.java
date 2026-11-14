package cr.ac.una.est.socialuna.ui;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Navegación central entre vistas FXML (Dev 1).
 *
 * Los controladores se inyectan por constructor (sin setters), así que el
 * {@link FXMLLoader} recibe una controllerFactory que resuelve cada clase
 * contra el registro de fábricas hecho en AppSocialUNA.
 *
 * Los FXML viven en este mismo paquete de recursos:
 * src/main/resources/cr/ac/una/est/socialuna/ui/
 */
public class NavegadorVistas {

    private final Stage stage;
    private final Map<Class<?>, Supplier<?>> fabricas = new HashMap<>();
    private Parent vistaAnterior;

    public NavegadorVistas(Stage stage) {
        this.stage = stage;
    }

    /** Registra cómo crear un controlador (cierra sobre servicios y navegador). */
    public <T> void registrarFabrica(Class<T> tipo, Supplier<T> fabrica) {
        fabricas.put(tipo, fabrica);
    }

    public void mostrarLogin() {
        cambiarEscena("Login.fxml");
    }

    public void mostrarRegistro() {
        cambiarEscena("Registro.fxml");
    }

    public void mostrarInicio() {
        cambiarEscena("Feed.fxml");
    }

    public void mostrarBusqueda() {
        cambiarEscena("Busqueda.fxml");
    }

    /**
     * Vista compartida para perfil propio o ajeno (usa Feed.fxml con PanelPerfil).
     * Dev 2: cargar el FXML, obtener el PanelPerfil y llamar mostrarPerfil(perfilId).
     */
    public void mostrarPerfil(String perfilId) {
        throw new UnsupportedOperationException("pendiente: PanelPerfil (Dev 2)");
    }

    /**
     * Panel de comunidad para una pestaña de Inicio (MURO, CARRERA o SEDE).
     * Devuelve el Parent sin cambiar la escena: Feed lo coloca en su TabPane.
     * Dev 4: cargar Comunidad.fxml con un ControladorComunidad del ámbito dado.
     */
    public Parent cargarComunidad(AmbitoPublicacion ambito) {
        throw new UnsupportedOperationException("pendiente: ControladorComunidad (Dev 4)");
    }

    public void volver() {
        if (vistaAnterior == null) {
            throw new IllegalStateException("No hay vista anterior a la que volver.");
        }
        Parent actual = stage.getScene().getRoot();
        stage.getScene().setRoot(vistaAnterior);
        vistaAnterior = actual;
    }

    // package-private para poder probarlo sin el toolkit de JavaFX
    Object crearControlador(Class<?> tipo) {
        Supplier<?> fabrica = fabricas.get(tipo);
        if (fabrica == null) {
            throw new IllegalStateException(
                    "Sin fábrica registrada para " + tipo.getName()
                    + ". Llama registrarFabrica(...) en AppSocialUNA.");
        }
        return fabrica.get();
    }

    private void cambiarEscena(String vista) {
        Parent nueva = cargarVista(vista);
        Scene scene = stage.getScene();
        if (scene == null) {
            stage.setScene(new Scene(nueva, 800, 600));
        } else {
            vistaAnterior = scene.getRoot();
            scene.setRoot(nueva);
        }
    }

    private Parent cargarVista(String vista) {
        URL url = getClass().getResource(vista);
        if (url == null) {
            throw new IllegalStateException(
                    "No existe la vista '" + vista + "' en cr/ac/una/est/socialuna/ui (FXML pendiente).");
        }
        FXMLLoader loader = new FXMLLoader(url);
        loader.setControllerFactory(this::crearControlador);
        try {
            return loader.load();
        } catch (IOException e) {
            throw new UncheckedIOException("Error cargando " + vista, e);
        }
    }
}
