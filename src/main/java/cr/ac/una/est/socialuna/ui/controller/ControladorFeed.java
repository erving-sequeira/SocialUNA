package cr.ac.una.est.socialuna.ui.controller;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.service.contract.IServicioComunidad;
import cr.ac.una.est.socialuna.service.contract.IServicioPublicacion;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import cr.ac.una.est.socialuna.ui.component.PanelPublicaciones;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Objects;

public class ControladorFeed {
    private final IServicioPublicacion servicioPublicacion;
    private final IServicioComunidad servicioComunidad;
    private final NavegadorVistas navegador;
    private final PanelPublicaciones panel;

    @FXML private VBox vistaFeed;
    @FXML private TabPane pestanas;
    @FXML private ScrollPane listaPublicaciones;
    @FXML private VBox contenedorPanel;
    @FXML private VBox formularioPublicacion;
    @FXML private Label tituloFormulario;
    @FXML private TextArea textoPublicacion;
    @FXML private RadioButton tipoNormal;
    @FXML private RadioButton tipoPregunta;
    @FXML private Label mensaje;

    //Recibe los servicios del muro, el navegador y el panel donde se muestran las publicaciones.
    public ControladorFeed(IServicioPublicacion servicioPublicacion, IServicioComunidad servicioComunidad,
                           NavegadorVistas navegador, PanelPublicaciones panel) {
        this.servicioPublicacion = Objects.requireNonNull(servicioPublicacion);
        this.servicioComunidad = Objects.requireNonNull(servicioComunidad);
        this.navegador = Objects.requireNonNull(navegador);
        this.panel = Objects.requireNonNull(panel);
    }

    //Carga las publicaciones de la pestaña activa. Si falla, muestra un aviso en esa misma pantalla.
    public void cargarFeed() {
        mensaje.setText("");
        try {
            var publicaciones = switch (muroSeleccionado()) {
                case MURO -> servicioPublicacion.obtenerFeed();
                case CARRERA -> servicioComunidad.obtenerPorCarrera(null);
                case SEDE -> servicioComunidad.obtenerPorSede();
            };
            panel.mostrar(publicaciones, false);
        } catch (RuntimeException error) {
            panel.mostrar(new ArrayList<>(), false);
            mostrarError(error.getMessage());
        }
    }

    //Abre el formulario en el muro activo y deja listo el campo para escribir.
    @FXML private void abrirPublicacion() {
        tituloFormulario.setText("Publicar en " + pestanas.getSelectionModel().getSelectedItem().getText());
        formularioPublicacion.setVisible(true);
        listaPublicaciones.setVvalue(0);
        textoPublicacion.requestFocus();
    }

    //Limpia el borrador y cierra el formulario de publicación.
    @FXML private void cancelarPublicacion() {
        formularioPublicacion.setVisible(false);
        textoPublicacion.clear();
        tipoNormal.setSelected(true);
        mensaje.setText("");
    }

    //Toma el texto y el tipo elegidos y los guarda en el muro que está activo.
    @FXML public void publicar() {
        String texto = textoPublicacion.getText();
        if (texto == null || texto.isBlank()) {
            mostrarError("Escribe algo antes de publicar.");
            return;
        }

        var tipo = tipoPregunta.isSelected() ? TipoPublicacion.PREGUNTA : TipoPublicacion.GENERAL;
        try {
            switch (muroSeleccionado()) {
                case MURO -> servicioPublicacion.publicar(texto, tipo);
                case CARRERA -> servicioComunidad.publicarEnCarrera(texto, tipo);
                case SEDE -> servicioComunidad.publicarEnSede(texto, tipo);
            }
            cancelarPublicacion();
            cargarFeed();
        } catch (RuntimeException error) {
            mostrarError(error.getMessage());
        }
    }

    //Recibe el ámbito y selecciona su pestaña en el muro.
    public void seleccionarMuro(AmbitoPublicacion ambito) {
        if (ambito == null) {
            mostrarError("Debes seleccionar un muro.");
            return;
        }

        String idPestana = switch (ambito) {
            case MURO -> "tabMuro";
            case CARRERA -> "tabCarrera";
            case SEDE -> "tabSede";
        };
        pestanas.getTabs().stream().filter(tab -> idPestana.equals(tab.getId())).findFirst()
                .ifPresentOrElse(pestanas.getSelectionModel()::select,
                        () -> mostrarError("No se encontró la pestaña solicitada."));
    }

    //Devuelve el ámbito de la pestaña activa para cargar o publicar en el lugar correcto.
    private AmbitoPublicacion muroSeleccionado() {
        return switch (pestanas.getSelectionModel().getSelectedItem().getId()) {
            case "tabCarrera" -> AmbitoPublicacion.CARRERA;
            case "tabSede" -> AmbitoPublicacion.SEDE;
            default -> AmbitoPublicacion.MURO;
        };
    }

    //Recibe el ID del autor y abre su perfil desde la publicación.
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

    //Conecta las tarjetas y actualiza el muro al entrar o cambiar de pestaña.
    @FXML public void initialize() {
        var anchoPestana = Bindings.max(92, pestanas.widthProperty().subtract(42).divide(pestanas.getTabs().size()));
        pestanas.tabMinWidthProperty().bind(anchoPestana);
        pestanas.tabMaxWidthProperty().bind(anchoPestana);
        formularioPublicacion.managedProperty().bind(formularioPublicacion.visibleProperty());
        mensaje.visibleProperty().bind(mensaje.textProperty().isNotEmpty());
        mensaje.managedProperty().bind(mensaje.visibleProperty());
        contenedorPanel.getChildren().add(panel.getContenedor());
        panel.alAbrirPerfil(this::abrirPerfilAutor);
        pestanas.getSelectionModel().selectedItemProperty().addListener((observable, anterior, nueva) -> {
            if (nueva != null) tituloFormulario.setText("Publicar en " + nueva.getText());
            listaPublicaciones.setVvalue(0);
            cargarFeed();
        });
        vistaFeed.sceneProperty().addListener((propiedad, anterior, actual) -> {
            if (actual != null) cargarFeed();
        });
    }

    //Recibe el texto del error y lo muestra sobre la lista de publicaciones.
    private void mostrarError(String texto) {
        mensaje.setText(texto == null || texto.isBlank() ? "Ocurrió un error al realizar la operación." : texto);
    }
}
