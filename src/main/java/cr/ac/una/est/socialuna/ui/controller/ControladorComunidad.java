package cr.ac.una.est.socialuna.ui.controller;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.service.contract.IServicioComunidad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import cr.ac.una.est.socialuna.ui.component.PanelPublicaciones;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.Objects;

//controlador de la vista de comunidad
//construye la interfaz directamente en Java, sin depender de un archivo FXML
//coordina la consulta de publicaciones y las acciones de comentarios
public class ControladorComunidad extends BorderPane {

    private final IServicioUsuarios servicioUsuarios;

    private final IServicioComunidad servicioComunidad;

    private final NavegadorVistas navegador;

    private final PanelPublicaciones panel;

    private AmbitoPublicacion ambitoSeleccionado;

    private String sedeFiltroId;

    //botones para seleccionar el ámbito visible
    private Button botonMuro;
    private Button botonCarrera;
    private Button botonSede;

    private TextField campoFiltroSede;

    //controles para redactar una publicación
    private TextArea campoTexto;
    private ComboBox<TipoPublicacion> selectorTipo;

    //texto informativo del ámbito seleccionado
    private Label mensajeAmbito;

    //construye el controlador, valida sus dependencias, crea la interfaz
    //y conecta las acciones del panel
    public ControladorComunidad(
            IServicioUsuarios servicioUsuarios,
            IServicioComunidad servicioComunidad,
            NavegadorVistas navegador,
            PanelPublicaciones panel) {

        this.servicioUsuarios = Objects.requireNonNull(servicioUsuarios, "El servicio de usuarios es obligatorio.");
        this.servicioComunidad = Objects.requireNonNull(servicioComunidad, "El servicio de comunidad es obligatorio.");
        this.navegador = Objects.requireNonNull(navegador, "El navegador de vistas es obligatorio.");
        this.panel = Objects.requireNonNull(panel, "El panel de publicaciones es obligatorio.");

        this.ambitoSeleccionado = AmbitoPublicacion.MURO;

        construirVista();
        initialize();
    }

    //construye los controles de la pantalla directamente en Java:
    //botones de ámbito, filtro de sede, lista de publicaciones y editor
    private void construirVista() {
        setPadding(new Insets(16));

        Label titulo = new Label("Comunidad");
        titulo.getStyleClass().add("titulo-comunidad");

        botonMuro = new Button("Muro");
        botonCarrera = new Button("Carrera");
        botonSede = new Button("Sede");

        botonMuro.setOnAction(evento -> cambiarAmbito(AmbitoPublicacion.MURO));
        botonCarrera.setOnAction(evento -> cambiarAmbito(AmbitoPublicacion.CARRERA));
        botonSede.setOnAction(evento -> cambiarAmbito(AmbitoPublicacion.SEDE));

        HBox botonesAmbito = new HBox(8, botonMuro, botonCarrera, botonSede);

        campoFiltroSede = new TextField();
        campoFiltroSede.setPromptText("ID de sede para filtrar (opcional)");

        Button botonAplicarFiltro = new Button("Filtrar");
        botonAplicarFiltro.setOnAction(evento -> cambiarFiltroSede(campoFiltroSede.getText()));

        HBox filaFiltro = new HBox(8, campoFiltroSede, botonAplicarFiltro);
        HBox.setHgrow(campoFiltroSede, Priority.ALWAYS);

        mensajeAmbito = new Label();

        VBox encabezado = new VBox(10, titulo, botonesAmbito, filaFiltro, mensajeAmbito);
        encabezado.setPadding(new Insets(0, 0, 12, 0));

        ScrollPane scrollPublicaciones = new ScrollPane(panel.getContenedor());
        scrollPublicaciones.setFitToWidth(true);
        scrollPublicaciones.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        campoTexto = new TextArea();
        campoTexto.setPromptText("Escribe una publicación...");
        campoTexto.setWrapText(true);
        campoTexto.setPrefRowCount(3);

        selectorTipo = new ComboBox<>();
        selectorTipo.getItems().setAll(TipoPublicacion.values());
        selectorTipo.setPromptText("Tipo");

        Button botonPublicar = new Button("Publicar");
        botonPublicar.setOnAction(evento -> publicar());

        HBox filaPublicar = new HBox(8, selectorTipo, botonPublicar);
        filaPublicar.setPadding(new Insets(8, 0, 0, 0));

        VBox editor = new VBox(8, campoTexto, filaPublicar);
        editor.setPadding(new Insets(12, 0, 0, 0));

        VBox contenido = new VBox(8, encabezado, scrollPublicaciones, editor);
        VBox.setVgrow(scrollPublicaciones, Priority.ALWAYS);

        setCenter(contenido);
    }

    //conecta las acciones de PanelPublicaciones con los métodos del
    //controlador y carga el contenido inicial
    public void initialize() {
        panel.alComentar(this::comentar);
        panel.alVerComentarios(this::mostrarComentarios);
        panel.alAbrirPerfil(navegador::mostrarPerfil);
        actualizarBotonesAmbito();
        cargarPublicaciones();
    }

    //cambia el ámbito que se está mostrando y recarga su contenido
    public void cambiarAmbito(AmbitoPublicacion ambito) {
        if (ambito == null) {
            mostrarError("Debe seleccionar un ámbito.");
            return;
        }
        ambitoSeleccionado = ambito;
        actualizarBotonesAmbito();
        cargarPublicaciones();
    }

    //obtiene del servicio las publicaciones del ámbito seleccionado
    //en Carrera aplica el filtro opcional de sede y en Sede consulta
    //las publicaciones de la sede configurada
    public void cargarPublicaciones() {
        try {
            if (ambitoSeleccionado == AmbitoPublicacion.CARRERA) {
                ArrayList<Publicacion> publicaciones = servicioComunidad.obtenerPorCarrera(sedeFiltroId);

                mensajeAmbito.setText("Publicaciones de la carrera");
                panel.mostrar(publicaciones, true);

            } else if (ambitoSeleccionado == AmbitoPublicacion.SEDE) {
                ArrayList<Publicacion> publicaciones = servicioComunidad.obtenerPorSede();

                mensajeAmbito.setText("Publicaciones de la sede");
                panel.mostrar(publicaciones, true);

            } else {
                //el contrato IServicioComunidad compartido solo define
                //operaciones para Carrera y Sede
                mensajeAmbito.setText("El muro general se carga desde la sección de inicio.");
                panel.mostrar(new ArrayList<>(), false);
            }
        } catch (RuntimeException error) {
            mostrarError(error.getMessage());
        }
    }

    //actualiza el filtro de sede. Un valor vacío elimina el filtro
    //si se está consultando Carrera, recarga las publicaciones
    public void cambiarFiltroSede(String sedeId) {
        if (sedeId == null || sedeId.trim().isEmpty()) {
            sedeFiltroId = null;
        } else {
            sedeFiltroId = sedeId.trim();
        }
        if (ambitoSeleccionado == AmbitoPublicacion.CARRERA) {
            cargarPublicaciones();
        }
    }

    //valida el texto y el tipo, solicita al servicio crear la publicación
    //en Carrera o Sede y actualiza la lista
    public void publicar() {
        String texto = campoTexto.getText();
        TipoPublicacion tipo = selectorTipo.getValue();

        if (texto == null || texto.trim().isEmpty()) {
            mostrarError("Escribe el texto de la publicación.");
            return;
        }

        if (tipo == null) {
            mostrarError("Selecciona el tipo de publicación.");
            return;
        }

        try {
            if (ambitoSeleccionado == AmbitoPublicacion.CARRERA) {
                servicioComunidad.publicarEnCarrera(texto.trim(), tipo);

            } else if (ambitoSeleccionado == AmbitoPublicacion.SEDE) {
                servicioComunidad.publicarEnSede(texto.trim(), tipo);

            } else {
                mostrarError("Selecciona Carrera o Sede para publicar desde esta vista.");
                return;
            }

            campoTexto.clear();
            cargarPublicaciones();

        } catch (RuntimeException error) {
            mostrarError(error.getMessage());
        }
    }

    //envía un comentario al servicio y recarga las publicaciones visibles
    public void comentar(String publicacionId, String texto) {
        try {
            servicioComunidad.comentar(publicacionId, texto);
            cargarPublicaciones();

        } catch (RuntimeException error) {
            mostrarError(error.getMessage());
        }
    }

    //obtiene los comentarios de una publicación y los muestra en su tarjeta
    public void mostrarComentarios(String publicacionId) {
        try {
            panel.mostrarComentarios(publicacionId, servicioComunidad.obtenerComentarios(publicacionId));
        } catch (RuntimeException error) {
            mostrarError(error.getMessage());
        }
    }

    //marca el botón correspondiente al ámbito seleccionado
    private void actualizarBotonesAmbito() {
        botonMuro.setDefaultButton(ambitoSeleccionado == AmbitoPublicacion.MURO);
        botonCarrera.setDefaultButton(ambitoSeleccionado == AmbitoPublicacion.CARRERA);
        botonSede.setDefaultButton(ambitoSeleccionado == AmbitoPublicacion.SEDE);
    }

    //presenta una ventana de error con el mensaje recibido
    private void mostrarError(String mensaje) {
        String texto = (mensaje == null || mensaje.isBlank()) ? "Ocurrió un error al procesar la operación." : mensaje;

        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Comunidad");
        alerta.setHeaderText("No se pudo completar la operación");
        alerta.setContentText(texto);
        alerta.showAndWait();
    }
}