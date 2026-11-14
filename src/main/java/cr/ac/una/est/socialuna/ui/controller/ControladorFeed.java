
package cr.ac.una.est.socialuna.ui.controller;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.service.contract.IServicioPublicacion;
import cr.ac.una.est.socialuna.service.contract.IServicioComunidad;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import cr.ac.una.est.socialuna.ui.component.PanelPublicaciones;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Objects;

public class ControladorFeed {

    private final IServicioUsuarios servicioUsuarios;
    private final IServicioPublicacion servicioPublicacion;
    private final IServicioComunidad servicioComunidad;
    private final NavegadorVistas navegador;
    private final PanelPublicaciones panel;

    @FXML private ScrollPane vistaFeed;

    @FXML
    private TabPane pestañas;

    @FXML
    private TextArea textoPublicacion;

    @FXML
    private VBox contenedorPanel;

    @FXML
    private Label mensaje;

    // Recibe los servicios, el navegador y el panel de publicaciones.
    // Guarda las dependencias que necesitamos para manejar el Feed.
    public ControladorFeed(
            IServicioUsuarios servicioUsuarios,
            IServicioPublicacion servicioPublicacion,
            IServicioComunidad servicioComunidad,
            NavegadorVistas navegador,
            PanelPublicaciones panel) {

        this.servicioUsuarios = Objects.requireNonNull(servicioUsuarios);
        this.servicioPublicacion = Objects.requireNonNull(servicioPublicacion);
        this.servicioComunidad = Objects.requireNonNull(servicioComunidad);
        this.navegador = Objects.requireNonNull(navegador);
        this.panel = Objects.requireNonNull(panel);
    }

    //Carga y muestra las publicaciones del muro que está seleccionado.
    @FXML
    public void cargarFeed() {

        try {
            mensaje.setText("");

            Tab seleccionada = pestañas.getSelectionModel().getSelectedItem();

            if (seleccionada == null) {
                return;
            }

            ArrayList<Publicacion> publicaciones;

            switch (seleccionada.getId()) {

                case "tabMuro":
                    publicaciones = servicioPublicacion.obtenerFeed();
                    break;

                case "tabCarrera":
                    publicaciones = servicioComunidad.obtenerPorCarrera(null);
                    break;

                case "tabSede":
                    publicaciones = servicioComunidad.obtenerPorSede();
                    break;

                default:
                    publicaciones = servicioPublicacion.obtenerFeed();
                    break;
            }

            panel.mostrar(publicaciones, false);

        } catch (RuntimeException error) {
            panel.mostrar(new ArrayList<>(), false);
            mostrarError(error.getMessage());
        }
    }

    //Publica el texto en el muro activo. Si se guarda, limpia el campo y recarga la lista.
    @FXML
    public void publicar() {

        String texto = textoPublicacion.getText();

        if (texto == null || texto.isBlank()) {
            mostrarError("Escribe algo antes de publicar.");
            return;
        }

        try {
            Tab seleccionada = pestañas.getSelectionModel().getSelectedItem();

            if (seleccionada == null) {
                mostrarError("Selecciona un muro para publicar.");
                return;
            }

            switch (seleccionada.getId()) {

                case "tabMuro":
                    servicioPublicacion.publicar(texto.strip());
                    break;

                case "tabCarrera":
                    servicioComunidad.publicarEnCarrera(
                            texto.strip(), TipoPublicacion.GENERAL);
                    break;

                case "tabSede":
                    servicioComunidad.publicarEnSede(
                            texto.strip(), TipoPublicacion.GENERAL);
                    break;

                default:
                    mostrarError("No se pudo identificar el muro seleccionado.");
                    return;
            }

            textoPublicacion.clear();
            cargarFeed();

        } catch (RuntimeException error) {
            mostrarError(error.getMessage());
        }
    }

    //Recibe el ámbito y selecciona la pestaña que le toca.
    public void seleccionarMuro(AmbitoPublicacion ambito) {

        if (ambito == null) {
            mostrarError("Debes seleccionar un muro.");
            return;
        }

        String idPestana;

        switch (ambito) {
            case MURO:
                idPestana = "tabMuro";
                break;

            case CARRERA:
                idPestana = "tabCarrera";
                break;

            case SEDE:
                idPestana = "tabSede";
                break;

            default:
                mostrarError("El muro seleccionado no es válido.");
                return;
        }

        for (Tab tab : pestañas.getTabs()) {
            if (idPestana.equals(tab.getId())) {
                pestañas.getSelectionModel().select(tab);
                return;
            }
        }

        mostrarError("No se encontró la pestaña solicitada.");
    }

    //Abre la búsqueda de estudiantes desde el muro.
    @FXML
    public void abrirBusqueda() {

        try {
            navegador.mostrarBusqueda();

        } catch (RuntimeException error) {
            mostrarError("No se pudo abrir la búsqueda de estudiantes.");
        }
    }

    //Abre el perfil del usuario que inició sesión.
    @FXML
    public void abrirMiPerfil() {

        try {
            Usuario usuario = servicioUsuarios.getUsuarioActual();

            if (usuario == null) {
                mostrarError("Debes iniciar sesión para ver tu perfil.");
                return;
            }

            navegador.mostrarPerfil(usuario.getId());


        } catch (RuntimeException error) {
            mostrarError("No se pudo abrir tu perfil.");
        }
    }

    //Recibe el ID del autor y abre su perfil desde el post.
    public void abrirPerfilAutor(String autorId) {

        if (autorId == null || autorId.isBlank()) {
            mostrarError("No se pudo identificar al autor.");
            return;
        }

        try {
            navegador.mostrarPerfil(autorId);


        } catch (RuntimeException error) {
            mostrarError("No se pudo abrir el perfil del autor.");
        }
    }

    //Conecta las acciones del panel y recarga las publicaciones al entrar o cambiar de pestaña.
    @FXML
    public void initialize() {

        contenedorPanel.getChildren().add(panel.getContenedor());

        panel.alAbrirPerfil(this::abrirPerfilAutor);

        pestañas.getSelectionModel().selectedItemProperty().addListener(
                (observable, anterior, nueva) -> cargarFeed()
        );
        vistaFeed.sceneProperty().addListener((propiedad, anterior, actual) -> {
            if (actual != null) cargarFeed();
        });
    }

    // Recibe el mensaje que queremos mostrar.
    // Actualiza la etiqueta para informarle al usuario si ocurrió algún problema.
    // No devuelve ningún valor.
    private void mostrarError(String mensaje) {

        if (mensaje == null || mensaje.isBlank()) {
            this.mensaje.setText("Ocurrió un error al realizar la operación.");
        } else {
            this.mensaje.setText(mensaje);
        }
    }
}
