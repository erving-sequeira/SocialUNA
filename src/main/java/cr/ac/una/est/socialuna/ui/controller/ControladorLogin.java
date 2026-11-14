
package cr.ac.una.est.socialuna.ui.controller;

import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import cr.ac.una.est.socialuna.ui.Tema;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Objects;

public class ControladorLogin {

    // Servicios y herramientas que necesitamos para iniciar sesión y cambiar de pantalla.
    private final IServicioUsuarios servicioUsuarios;
    private final NavegadorVistas navegador;

    // Campos y elementos gráficos que están conectados con Login.fxml.
    @FXML
    private TextField nombreUsuario;

    @FXML
    private PasswordField contrasena;

    @FXML
    private Label mensaje;

    // Recibe el servicio de usuarios y el navegador de pantallas.
    // Guarda ambas dependencias para utilizarlas durante el inicio de sesión.
    public ControladorLogin(IServicioUsuarios servicioUsuarios,
                            NavegadorVistas navegador) {

        this.servicioUsuarios = Objects.requireNonNull(servicioUsuarios);
        this.navegador = Objects.requireNonNull(navegador);
    }

    // Recibe los datos del formulario de login.
    // Valida las credenciales y abre la pantalla de inicio si son correctas.
    // No devuelve ningún valor, pero muestra un mensaje si ocurre algún error.
    @FXML
    public void ingresar() {

        mensaje.setText("");

        try {
            servicioUsuarios.iniciarSesion(
                    nombreUsuario.getText(),
                    contrasena.getText()
            );

        } catch (IllegalArgumentException | IllegalStateException error) {

            contrasena.clear();
            mostrarError(error.getMessage());
            return;
        }

        contrasena.clear();

        try {
            navegador.mostrarInicio(this::cerrarSesion);

        } catch (RuntimeException error) {

            servicioUsuarios.cerrarSesion();

            mostrarError("No se pudo abrir Inicio. Inténtalo de nuevo.");

            System.getLogger(getClass().getName()).log(
                    System.Logger.Level.WARNING,
                    "Error al abrir Inicio",
                    error
            );
        }
    }

    // No recibe parámetros.
    // Abre la pantalla de registro para que un usuario pueda crear su cuenta.
    // No devuelve ningún valor.
    @FXML
    public void abrirRegistro() {

        navegador.mostrarRegistro();
    }

    // No recibe parámetros.
    // Se ejecuta cuando JavaFX carga Login.fxml y configura el mensaje de error.
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

    // Recibe el mensaje que queremos mostrarle al usuario.
    // Actualiza la etiqueta del formulario para informar si ocurrió algún problema.
    // No devuelve ningún valor.
    private void mostrarError(String mensaje) {

        this.mensaje.setText(mensaje);
    }

    // No recibe parámetros.
    // Alterna entre el tema claro y oscuro de la aplicación.
    // No devuelve ningún valor.
    @FXML
    private void alternarTema() {

        Tema.alternar();
    }

    // No recibe parámetros.
    // Cierra la sesión actual y devuelve al usuario a la pantalla de login.
    // No devuelve ningún valor.
    private void cerrarSesion() {

        servicioUsuarios.cerrarSesion();
        navegador.mostrarLogin();
    }
}
