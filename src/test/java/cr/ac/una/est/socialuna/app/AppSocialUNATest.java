package cr.ac.una.est.socialuna.app;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba opcional de la ventana real; necesita un entorno gráfico. */
@EnabledIfSystemProperty(named="pruebas.javafx",matches="true")
class AppSocialUNATest {
    @Test void iniciaLaVentanaSocialUna() throws Exception {
        CompletableFuture<Void> inicio = new CompletableFuture<>();
        Platform.startup(() -> { Platform.setImplicitExit(false); inicio.complete(null); });
        inicio.get(10,TimeUnit.SECONDS);
        CompletableFuture<Void> resultado = new CompletableFuture<>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                stage.setOpacity(0);
                new AppSocialUNA().start(stage);
                assertTrue(stage.isShowing()); assertEquals("SocialUNA",stage.getTitle());
                assertEquals(800,stage.getScene().getWidth(),1); assertEquals(600,stage.getScene().getHeight(),1);
                assertInstanceOf(StackPane.class,stage.getScene().getRoot());
                resultado.complete(null);
            } catch(Throwable error) { resultado.completeExceptionally(error); }
            finally { stage.close(); }
        });
        resultado.get(10,TimeUnit.SECONDS);
    }
}
