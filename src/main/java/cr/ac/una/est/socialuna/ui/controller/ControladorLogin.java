package cr.ac.una.est.socialuna.ui.controller;

import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Objects;

public class ControladorLogin {
    private final IServicioUsuarios usuarios;
    private final NavegadorVistas navegador;

    @FXML private TextField nombreUsuario;
    @FXML private PasswordField contrasena;
    @FXML private Label mensaje;

    //Recibe el servicio de usuarios y el navegador para entrar o cambiar de pantalla.
    public ControladorLogin(IServicioUsuarios usuarios, NavegadorVistas navegador) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.navegador = Objects.requireNonNull(navegador);
    }

    //Oculta el espacio del mensaje mientras no haya nada que mostrar.
    @FXML private void initialize() {
        mensaje.visibleProperty().bind(mensaje.textProperty().isNotEmpty());
        mensaje.managedProperty().bind(mensaje.visibleProperty());
    }

    //Toma el usuario y la contraseña del formulario. Abre Inicio si funcionan o muestra el error.
    @FXML private void ingresar() {
        mensaje.setText("");
        try {
            usuarios.iniciarSesion(nombreUsuario.getText(), contrasena.getText());
        } catch (IllegalArgumentException | IllegalStateException error) {
            contrasena.clear();
            mensaje.setText(error.getMessage());
            return;
        }

        contrasena.clear();
        try {
            navegador.mostrarInicio(this::cerrarSesion);
        } catch (RuntimeException error) {
            usuarios.cerrarSesion();
            mensaje.setText("No se pudo abrir Inicio. Inténtalo de nuevo.");
            System.getLogger(getClass().getName()).log(System.Logger.Level.WARNING, "Error al abrir Inicio", error);
        }
    }

    //Abre el formulario de registro desde el login.
    @FXML private void abrirRegistro() {
        navegador.mostrarRegistro();
    }

    //Cierra la sesión y vuelve al login.
    private void cerrarSesion() {
        usuarios.cerrarSesion();
        navegador.mostrarLogin();
    }
}
