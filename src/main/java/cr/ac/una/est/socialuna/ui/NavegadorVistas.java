package cr.ac.una.est.socialuna.ui;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NavegadorVistas {

    private final Stage stage;
    private final Map<Class<?>, Supplier<?>> fabricas = new HashMap<>();
    private Parent vistaAnterior;

    //Recibe la ventana donde se van a mostrar las pantallas.
    public NavegadorVistas(Stage stage) {
        this.stage = stage;
    }

    //Recibe el tipo de controlador y la función que lo crea. Los guarda para cargar el FXML después.
    public <T> void registrarFabrica(Class<T> tipo, Supplier<T> fabrica) {
        fabricas.put(Objects.requireNonNull(tipo), Objects.requireNonNull(fabrica));
    }

    //Abre el login y borra la vista anterior.
    public void mostrarLogin() {
        cambiarEscena("Login.fxml");
        vistaAnterior = null;
    }

    //Abre el formulario de registro.
    public void mostrarRegistro() {
        cambiarEscena("Registro.fxml");
    }

    //Abre la pantalla de Inicio desde Feed.fxml.
    public void mostrarInicio() {
        cambiarEscena("Feed.fxml");
    }

    //Recibe la acción de cerrar sesión. Abre Inicio o, si falta Feed.fxml, muestra una pantalla temporal con ese botón.
    public void mostrarInicio(Runnable cerrarSesion) {
        Objects.requireNonNull(cerrarSesion);
        if (recursoVista("Feed.fxml") != null) {
            mostrarInicio();
        } else {
            var titulo = new Label("Inicio");
            titulo.getStyleClass().add("titulo");
            var aviso = new Label("Sesión iniciada. La pantalla de Inicio está en preparación.");
            aviso.setWrapText(true);
            var salir = new Button("Cerrar sesión");
            salir.setId("btnCerrarSesion");
            salir.setOnAction(evento -> cerrarSesion.run());
            var vista = new VBox(14, titulo, aviso, salir);
            vista.setAlignment(Pos.CENTER);
            vista.getStyleClass().add("acceso");
            vista.getStylesheets().add(getClass().getResource("css/acceso.css").toExternalForm());
            presentar(vista);
        }
        vistaAnterior = null;
    }

    //Abre la pantalla de búsqueda de perfiles.
    public void mostrarBusqueda() {
        cambiarEscena("Busqueda.fxml");
    }

    //Recibe el ID del perfil. Falta abrirlo con PanelPerfil dentro de Feed.fxml y por ahora da error.
    public void mostrarPerfil(String perfilId) {
        throw new UnsupportedOperationException("pendiente: PanelPerfil (Dev 2)");
    }

    //Recibe el ámbito para un panel de Inicio. Falta cargar Comunidad.fxml y por ahora da error.
    public Parent cargarComunidad(AmbitoPublicacion ambito) {
        throw new UnsupportedOperationException("pendiente: ControladorComunidad (Dev 4)");
    }

    //Recupera la vista anterior. Da error si no quedó ninguna guardada.
    public void volver() {
        if (vistaAnterior == null) {
            throw new IllegalStateException("No hay vista anterior a la que volver.");
        }
        stage.getScene().setRoot(vistaAnterior);
        vistaAnterior = null;
    }

    //Recibe el tipo y devuelve un controlador creado por su fábrica. Da error si no hay una fábrica válida.
    Object crearControlador(Class<?> tipo) {
        Supplier<?> fabrica = fabricas.get(tipo);
        if (fabrica == null) {
            throw new IllegalStateException(
                    "Sin fábrica registrada para " + tipo.getName()
                    + ". Llama registrarFabrica(...) en AppSocialUNA.");
        }
        Object controlador = fabrica.get();
        if (!tipo.isInstance(controlador)) {
            throw new IllegalStateException("La fábrica no devolvió un controlador de tipo " + tipo.getName() + ".");
        }
        return controlador;
    }

    //Recibe el nombre del FXML, lo carga y muestra su pantalla.
    private void cambiarEscena(String vista) {
        presentar(cargarVista(vista));
    }

    //Recibe la raíz de la vista y la muestra, guardando la anterior si ya había una.
    private void presentar(Parent nueva) {
        Scene scene = stage.getScene();
        if (scene == null) {
            stage.setScene(new Scene(nueva, 800, 600));
        } else {
            vistaAnterior = scene.getRoot();
            scene.setRoot(nueva);
        }
    }

    //Recibe el nombre del FXML y devuelve su URL, o null si no lo encuentra.
    private URL recursoVista(String vista) {
        return getClass().getResource("fxml/" + vista);
    }

    //Recibe el nombre del FXML y devuelve su raíz con el controlador ya conectado.
    private Parent cargarVista(String vista) {
        URL url = recursoVista(vista);
        if (url == null) {
            throw new IllegalStateException(
                    "No existe la vista '" + vista + "' en cr/ac/una/est/socialuna/ui/fxml.");
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
