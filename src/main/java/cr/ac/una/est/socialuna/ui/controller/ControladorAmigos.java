
package cr.ac.una.est.socialuna.ui.controller;

import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Objects;

public class ControladorAmigos {

    private final IServicioUsuarios servicioUsuarios;
    private final NavegadorVistas navegador;

    @FXML
    private TextField campoBusqueda;

    @FXML
    private VBox contenedorResultados;

    @FXML
    private Label mensaje;

    // Recibe el servicio de usuarios y el navegador de pantallas.
    // Guarda ambas dependencias para buscar estudiantes y abrir sus perfiles.
    public ControladorAmigos(IServicioUsuarios servicioUsuarios,
                             NavegadorVistas navegador) {

        this.servicioUsuarios = Objects.requireNonNull(servicioUsuarios);
        this.navegador = Objects.requireNonNull(navegador);
    }

    // Recibe el texto escrito en el campo de búsqueda.
    // Busca estudiantes por nombre o nombre de usuario y muestra los resultados.
    // No devuelve ningún valor, solamente actualiza la pantalla.
    @FXML
    public void buscarPorNombreUsuario() {

        contenedorResultados.getChildren().clear();
        mensaje.setText("");

        String texto = campoBusqueda.getText();

        if (texto == null || texto.isBlank()) {
            mostrarError("Escribe un nombre o usuario para realizar la búsqueda.");
            return;
        }

        try {
            Usuario usuarioActual = servicioUsuarios.getUsuarioActual();

            if (usuarioActual == null) {
                mostrarError("Debes iniciar sesión para buscar estudiantes.");
                return;
            }

            ArrayList<Usuario> usuariosEncontrados =
                    servicioUsuarios.buscarUsuarios(texto.strip());

            for (Usuario usuario : usuariosEncontrados) {

                // Evitamos mostrar al usuario que inició sesión.
                if (!usuario.getId().equals(usuarioActual.getId())) {

                    HBox fila = new HBox(12);

                    Label nombre = new Label(
                            usuario.getNombre() + "  @" + usuario.getNombreUsuario()
                    );

                    Button botonPerfil = new Button("Ver perfil");

                    botonPerfil.setOnAction(evento ->
                            abrirPerfil(usuario.getId())
                    );

                    fila.getChildren().addAll(nombre, botonPerfil);

                    contenedorResultados.getChildren().add(fila);
                }
            }

            if (contenedorResultados.getChildren().isEmpty()) {
                mensaje.setText("No se encontraron otros estudiantes.");
            }

        } catch (RuntimeException error) {
            mostrarError(error.getMessage() == null
                    ? "Ocurrió un error al buscar estudiantes."
                    : error.getMessage());
        }
    }

    // Recibe el ID del estudiante cuyo perfil se quiere consultar.
    // Le pide al navegador que abra el perfil seleccionado.
    // No devuelve ningún valor y muestra un mensaje si todavía no está disponible.
    public void abrirPerfil(String perfilId) {

        if (perfilId == null || perfilId.isBlank()) {
            mostrarError("No se pudo identificar el perfil seleccionado.");
            return;
        }

        try {
            navegador.mostrarPerfil(perfilId);

        } catch (UnsupportedOperationException error) {
            mostrarError("La pantalla de perfiles todavía está en desarrollo.");

        } catch (RuntimeException error) {
            mostrarError("No se pudo abrir el perfil seleccionado.");
        }
    }

    // No recibe parámetros.
    // Se ejecuta cuando JavaFX carga la pantalla y configura el mensaje de error.
    // No devuelve ningún valor.
    @FXML
    public void initialize() {

        mensaje.visibleProperty().bind(
                mensaje.textProperty().isNotEmpty()
        );

        mensaje.managedProperty().bind(
                mensaje.visibleProperty()
        );
    }

    // Recibe el mensaje que queremos mostrar en la pantalla.
    // Actualiza la etiqueta para informarle al usuario si ocurrió algún problema.
    // No devuelve ningún valor.
    private void mostrarError(String mensaje) {

        this.mensaje.setText(mensaje);
    }
}
