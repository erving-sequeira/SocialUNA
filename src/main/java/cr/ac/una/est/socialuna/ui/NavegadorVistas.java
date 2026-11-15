package cr.ac.una.est.socialuna.ui;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.ui.component.PanelPerfil;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class NavegadorVistas {
    private static final PseudoClass SELECCIONADO = PseudoClass.getPseudoClass("selected");

    private final Stage stage;
    private final Map<Class<?>, Supplier<?>> fabricas = new HashMap<>();

    private BorderPane espacioSocial;
    private GridPane menuInferior;
    private Parent muro;
    private Parent busqueda;
    private String perfilPropioId;

    //Recibe la ventana donde se van a mostrar las pantallas.
    public NavegadorVistas(Stage stage) {
        this.stage = stage;
    }

    //Recibe un tipo de controlador y la función que lo crea para poder cargar sus vistas.
    public <T> void registrarFabrica(Class<T> tipo, Supplier<T> fabrica) {
        fabricas.put(Objects.requireNonNull(tipo), Objects.requireNonNull(fabrica));
    }

    //Abre el login y descarta las vistas de la sesión anterior.
    public void mostrarLogin() {
        presentar(cargarVista("Login.fxml"));
        espacioSocial = null;
        menuInferior = null;
        muro = null;
        busqueda = null;
        perfilPropioId = null;
    }

    //Abre el registro desde el login.
    public void mostrarRegistro() {
        presentar(cargarVista("Registro.fxml"));
    }

    //Recibe el ID de quien entró y abre el muro con la navegación de esa sesión.
    public void mostrarInicio(String perfilId) {
        perfilPropioId = Objects.requireNonNull(perfilId);
        mostrarInicio();
    }

    //Abre el muro y conserva la pestaña elegida durante esta sesión.
    public void mostrarInicio() {
        if (muro == null) muro = cargarVista("Feed.fxml");
        mostrarSeccion(muro, 0);
    }

    //Abre la búsqueda y conserva la consulta que estaba escrita.
    public void mostrarBusqueda() {
        if (busqueda == null) busqueda = cargarVista("Busqueda.fxml");
        mostrarSeccion(busqueda, 1);
    }

    //Recibe el ID del perfil. Si es ajeno, muestra el panel sin cambiar la sección activa.
    public void mostrarPerfil(String perfilId) {
        var panel = PanelPerfil.class.cast(crearControlador(PanelPerfil.class));
        var vista = panel.mostrarPerfil(perfilId);
        if (perfilPropioId.equals(perfilId)) {
            mostrarSeccion(vista, 2);
        } else {
            espacioSocial.setCenter(vista);
        }
    }

    //Recibe el ámbito para un panel de Inicio. Falta cargar Comunidad.fxml y por ahora da error.
    public Parent cargarComunidad(AmbitoPublicacion ambito) {
        throw new UnsupportedOperationException("pendiente: ControladorComunidad (Dev 4)");
    }

    //Recibe la vista y su posición en el menú. Cambia el contenido y marca la sección activa.
    private void mostrarSeccion(Parent vista, int seccion) {
        if (espacioSocial == null) {
            espacioSocial = new BorderPane();
            espacioSocial.setId("espacioSocial");
            espacioSocial.getStyleClass().addAll("acceso", "espacio-social");
            espacioSocial.getStylesheets().add(getClass().getResource("css/acceso.css").toExternalForm());
            menuInferior = new GridPane();
            menuInferior.setId("menuInferior");
            menuInferior.getStyleClass().add("menu-inferior");
            menuInferior.addRow(0,
                    opcion("Muro", "btnMuro", "oct-home-16", this::mostrarInicio),
                    opcion("Buscar estudiantes", "btnBuscarEstudiantes", "oct-search-16", this::mostrarBusqueda),
                    opcion("Mi perfil", "btnMiPerfil", "oct-person-16", () -> mostrarPerfil(perfilPropioId)));
            for (int i = 0; i < menuInferior.getChildren().size(); i++) {
                var columna = new ColumnConstraints();
                columna.setPercentWidth(100.0 / menuInferior.getChildren().size());
                menuInferior.getColumnConstraints().add(columna);
            }
            espacioSocial.setBottom(menuInferior);
        }

        for (int i = 0; i < menuInferior.getChildren().size(); i++) {
            menuInferior.getChildren().get(i).pseudoClassStateChanged(SELECCIONADO, i == seccion);
        }
        if (espacioSocial.getCenter() != vista) espacioSocial.setCenter(vista);
        presentar(espacioSocial);
    }

    //Recibe el texto, ID, icono y acción. Devuelve un botón que llena su columna del menú.
    private Button opcion(String texto, String id, String icono, Runnable accion) {
        var boton = new Button(texto, new FontIcon(icono));
        boton.setId(id);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.getStyleClass().add("opcion-menu");
        boton.setOnAction(evento -> accion.run());
        return boton;
    }

    //Recibe un tipo y devuelve el controlador creado por su fábrica registrada.
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

    //Recibe la raíz y la muestra en la ventana, creando la escena si hace falta.
    private void presentar(Parent nueva) {
        Scene scene = stage.getScene();
        if (scene == null) {
            stage.setScene(new Scene(nueva, 800, 600));
        } else if (scene.getRoot() != nueva) {
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
