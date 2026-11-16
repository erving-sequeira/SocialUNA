package cr.ac.una.est.socialuna.ui.controller;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import cr.ac.una.est.socialuna.ui.Tema;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.util.Objects;
import java.util.function.Function;

public class ControladorRegistro {
    private final IServicioUsuarios usuarios;
    private final NavegadorVistas navegador;

    @FXML private TextField nombre;
    @FXML private TextField nombreUsuario;
    @FXML private PasswordField contrasena;
    @FXML private ComboBox<Carrera> carrera;
    @FXML private ComboBox<SedeCampus> sede;
    @FXML private Button btnRegistrar;
    @FXML private Label mensaje;

    //Recibe el servicio de usuarios y el navegador para crear cuentas y volver al login.
    public ControladorRegistro(IServicioUsuarios usuarios, NavegadorVistas navegador) {
        this.usuarios = Objects.requireNonNull(usuarios);
        this.navegador = Objects.requireNonNull(navegador);
    }

    //Carga las carreras y deja elegir sede solo después de escoger una carrera.
    @FXML private void initialize() {
        mensaje.visibleProperty().bind(mensaje.textProperty().isNotEmpty());
        mensaje.managedProperty().bind(mensaje.visibleProperty());
        configurarCatalogo(carrera, Carrera::getNombre);
        configurarCatalogo(sede, SedeCampus::getNombre);
        carrera.getItems().setAll(usuarios.listarCarreras());
        sede.setPromptText("Elige primero una carrera");
        sede.disableProperty().bind(Bindings.isEmpty(sede.getItems()));
        btnRegistrar.disableProperty().bind(carrera.valueProperty().isNull().or(sede.valueProperty().isNull()));
        carrera.valueProperty().addListener((propiedad, anterior, seleccion) -> cargarSedes(seleccion));
        if (carrera.getItems().isEmpty() || usuarios.listarSedes().isEmpty()) {
            mensaje.setText("El registro requiere al menos una carrera y una sede activas.");
        }
    }

    //Recibe la carrera elegida, limpia la sede anterior y carga solo las opciones compatibles.
    private void cargarSedes(Carrera seleccion) {
        sede.getSelectionModel().clearSelection();
        sede.setValue(null);
        if (seleccion == null) {
            sede.getItems().clear();
            sede.setPromptText("Elige primero una carrera");
            mensaje.setText("");
            return;
        }

        sede.getItems().setAll(usuarios.listarSedesPorCarrera(seleccion.getId()));
        sede.setPromptText("Selecciona tu sede");
        mensaje.setText(sede.getItems().isEmpty() ? "Esta carrera no tiene sedes activas." : "");
    }

    //Toma los datos del formulario, crea la cuenta y vuelve al login si todo sale bien.
    @FXML private void registrar() {
        Carrera seleccionCarrera = carrera.getValue();
        SedeCampus seleccionSede = sede.getValue();
        if (seleccionCarrera == null || seleccionSede == null) {
            mensaje.setText("Selecciona tu carrera y tu sede.");
            return;
        }

        try {
            usuarios.registrar(nombre.getText(), nombreUsuario.getText(), contrasena.getText(),
                    seleccionCarrera.getId(), seleccionSede.getId());
        } catch (IllegalArgumentException | IllegalStateException error) {
            mensaje.setText(error.getMessage());
            return;
        }
        contrasena.clear();
        navegador.mostrarLogin();
    }

    //Regresa al login desde el registro.
    @FXML private void volverLogin() {
        navegador.mostrarLogin();
    }

    //Alterna entre el tema claro y el oscuro.
    @FXML private void alternarTema() {
        Tema.alternar();
    }

    //Recibe un combo y una función para sacar el nombre. Usa ese nombre en las opciones y en la selección.
    private static <T> void configurarCatalogo(ComboBox<T> combo, Function<T, String> nombre) {
        combo.setCellFactory(lista -> celda(nombre));
        combo.setButtonCell(celda(nombre));
    }

    //Recibe la función que saca el nombre y devuelve una celda que lo muestra.
    private static <T> ListCell<T> celda(Function<T, String> nombre) {
        return new ListCell<>() {
            //Recibe el valor y si la celda está vacía. Pone el nombre o limpia el texto.
            @Override protected void updateItem(T valor, boolean vacia) {
                super.updateItem(valor, vacia);
                setText(vacia || valor == null ? null : nombre.apply(valor));
            }
        };
    }
}
