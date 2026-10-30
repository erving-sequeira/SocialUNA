package cr.ac.una.est.socialuna;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AppSocialUNA extends Application {
    @Override
    public void start(Stage stage) {
        stage.setTitle("SocialUNA");
        stage.setScene(new Scene(new StackPane(), 800, 600));
        stage.show();
    }
}
