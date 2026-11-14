package cr.ac.una.est.socialuna.app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;

public class AppSocialUNA extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("SocialUNA");
        // Dev 2-4: registrar cada controlador aquí, p. ej.
        //   navegador.registrarFabrica(ControladorLogin.class,
        //       () -> new ControladorLogin(servicioUsuarios, navegador));
        // y reemplazar la escena vacía por navegador.mostrarLogin() cuando exista Login.fxml.
        NavegadorVistas navegador = new NavegadorVistas(stage);
        stage.setScene(new Scene(new StackPane(), 800, 600));
        stage.show();
    }
}
