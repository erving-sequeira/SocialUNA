package cr.ac.una.est.socialuna.ui.controller;

import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Objects;

public class ControladorAmigos {
    private final IServicioUsuarios servicioUsuarios;
    private final IServicioAmistad servicioAmistad;
    private final NavegadorVistas navegador;

    @FXML private ScrollPane vistaBusqueda;
    @FXML private TextField campoBusqueda;
    @FXML private VBox contenedorResultados;
    @FXML private VBox contenedorAmigos;
    @FXML private Label mensaje;

    //Recibe los servicios y el navegador para buscar estudiantes, listar amigos y abrir perfiles.
    public ControladorAmigos(IServicioUsuarios servicioUsuarios, IServicioAmistad servicioAmistad,
                             NavegadorVistas navegador) {
        this.servicioUsuarios = Objects.requireNonNull(servicioUsuarios);
        this.servicioAmistad = Objects.requireNonNull(servicioAmistad);
        this.navegador = Objects.requireNonNull(navegador);
    }

    //Lee el buscador y muestra a los demás estudiantes que coinciden por nombre o usuario.
    @FXML public void buscarPorNombreUsuario() {
        contenedorResultados.getChildren().clear();
        mensaje.setText("");
        String texto = campoBusqueda.getText();
        if (texto == null || texto.isBlank()) {
            mensaje.setText("Escribe un nombre o usuario para realizar la búsqueda.");
            return;
        }
        Usuario actual = servicioUsuarios.getUsuarioActual();
        if (actual == null) {
            mensaje.setText("Debes iniciar sesión para buscar estudiantes.");
            return;
        }

        var encontrados = servicioUsuarios.buscarUsuarios(texto.strip()).stream()
                .filter(usuario -> !usuario.getId().equals(actual.getId()))
                .toList();
        mostrarPerfiles(contenedorResultados, encontrados, "No se encontraron otros estudiantes.");
    }

    //Recibe el ID del estudiante y abre su perfil, o muestra un aviso si no se puede.
    public void abrirPerfil(String perfilId) {
        mensaje.setText("");
        try {
            navegador.mostrarPerfil(perfilId);
        } catch (IllegalArgumentException | IllegalStateException error) {
            mensaje.setText(error.getMessage());
        }
    }

    //Oculta el aviso vacío y actualiza los amigos al abrir la búsqueda o volver a ella.
    @FXML public void initialize() {
        mensaje.visibleProperty().bind(mensaje.textProperty().isNotEmpty());
        mensaje.managedProperty().bind(mensaje.visibleProperty());
        vistaBusqueda.sceneProperty().addListener((propiedad, anterior, actual) -> {
            if (actual != null) cargarAmigos();
        });
    }

    //Carga y muestra los amigos de la cuenta que está en sesión.
    private void cargarAmigos() {
        mostrarPerfiles(contenedorAmigos, servicioAmistad.listarMisAmigos(), "Todavía no tienes amigos.");
    }

    //Vuelve al muro que estaba abierto antes de buscar.
    @FXML private void volver() {
        navegador.volver();
    }

    //Recibe la lista, el contenedor y el texto vacío. Dibuja los botones para abrir cada perfil.
    private void mostrarPerfiles(VBox contenedor, List<Usuario> perfiles, String textoVacio) {
        contenedor.getChildren().clear();
        if (perfiles.isEmpty()) {
            contenedor.getChildren().add(new Label(textoVacio));
            return;
        }
        for (Usuario usuario : perfiles) {
            Label nombre = new Label(usuario.getNombre() + "  @" + usuario.getNombreUsuario());
            nombre.setWrapText(true);
            Button botonPerfil = new Button("Ver perfil");
            botonPerfil.setOnAction(evento -> abrirPerfil(usuario.getId()));
            contenedor.getChildren().add(new HBox(12, nombre, botonPerfil));
        }
    }
}
