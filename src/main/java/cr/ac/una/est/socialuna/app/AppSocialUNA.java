package cr.ac.una.est.socialuna.app;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.persistence.GestorJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioCarrerasJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioSedesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioUsuariosJSON;
import cr.ac.una.est.socialuna.service.ServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import cr.ac.una.est.socialuna.ui.controller.ControladorLogin;
import cr.ac.una.est.socialuna.ui.controller.ControladorRegistro;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AppSocialUNA extends Application {
    private final Path directorioDatos;

    //Deja lista la app para leer y guardar todo en la carpeta datos.
    public AppSocialUNA() {
        this(Path.of("datos"));
    }

    //Usa la carpeta de datos que se le pasa al crear la app.
    AppSocialUNA(Path directorioDatos) {
        this.directorioDatos = directorioDatos;
    }

    //Recibe la ventana de JavaFX, carga los datos y muestra el login o un aviso si algo falla.
    @Override
    public void start(Stage stage) {
        stage.setTitle("SocialUNA");
        stage.setMinWidth(520);
        stage.setMinHeight(560);
        ServicioUsuarios usuarios;
        try {
            usuarios = cargarUsuarios();
        } catch (IllegalStateException error) {
            var mensaje = new Label("No se pudieron cargar los datos. " + error.getMessage());
            mensaje.setWrapText(true);
            var vista = new VBox(12, mensaje);
            vista.setAlignment(Pos.CENTER);
            vista.setStyle("-fx-padding: 30;");
            stage.setScene(new Scene(vista, 800, 600));
            stage.show();
            return;
        }

        var navegador = new NavegadorVistas(stage);
        navegador.registrarFabrica(ControladorLogin.class, () -> new ControladorLogin(usuarios, navegador));
        navegador.registrarFabrica(ControladorRegistro.class, () -> new ControladorRegistro(usuarios, navegador));
        navegador.mostrarLogin();
        stage.show();
    }

    //Abre los repositorios, crea catálogos de ejemplo si faltan y devuelve el servicio de usuarios.
    private ServicioUsuarios cargarUsuarios() {
        var gestor = new GestorJSON(directorioDatos);
        var usuarios = new RepositorioUsuariosJSON(gestor);
        var carreras = new RepositorioCarrerasJSON(gestor);
        var sedes = new RepositorioSedesJSON(gestor);

        if (Files.notExists(directorioDatos.resolve("carreras.json"))) {
            carreras.guardarCarreras(List.of(new Carrera("informatica", "Informática", true),
                    new Carrera("administracion", "Administración", true)));
        }
        if (Files.notExists(directorioDatos.resolve("sedes.json"))) {
            sedes.guardarSedes(List.of(new SedeCampus("central", "Campus Omar Dengo", null, "Campus", true),
                    new SedeCampus("siua", "Sede Interuniversitaria de Alajuela", "SIUA", "Sede", true)));
        }
        return new ServicioUsuarios(usuarios, sedes, carreras);
    }
}
