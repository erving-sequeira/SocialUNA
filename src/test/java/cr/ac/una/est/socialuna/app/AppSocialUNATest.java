package cr.ac.una.est.socialuna.app;

import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import cr.ac.una.est.socialuna.model.entity.*;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.component.PanelPublicaciones;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

/** Prueba opcional de la ventana real; necesita un entorno gráfico. */
@EnabledIfSystemProperty(named="pruebas.javafx",matches="true")
class AppSocialUNATest {
    @BeforeAll static void iniciarJavaFx() throws Exception {
        CompletableFuture<Void> inicio = new CompletableFuture<>();
        Platform.startup(() -> { Platform.setImplicitExit(false); inicio.complete(null); });
        inicio.get(10,TimeUnit.SECONDS);
    }

    @Test void iniciaLaVentanaSocialUna() throws Exception {
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

    @Test void panelRespetaElAmbitoYEnviaLasAccionesALaPublicacionCorrecta() throws Exception {
        CompletableFuture<Void> resultado = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                var autora = new Usuario("ana", "Ana", "ana", "a".repeat(64), "c1", "s1");
                var usuarios = (IServicioUsuarios) Proxy.newProxyInstance(IServicioUsuarios.class.getClassLoader(),
                        new Class<?>[]{IServicioUsuarios.class}, (proxy, metodo, argumentos) -> {
                            assertEquals("obtenerPerfil", metodo.getName());
                            return autora.getId().equals(argumentos[0]) ? autora : null;
                        });
                var panel = new PanelPublicaciones(usuarios);
                var muro = new Publicacion("muro", "ana", "Hola", 1, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null);
                panel.mostrar(new ArrayList<>(List.of(muro)), false);
                var tarjetaMuro = (VBox) panel.getContenedor().getChildren().getFirst();
                var autor = (Hyperlink) tarjetaMuro.getChildren().getFirst();
                assertTrue(autor.isDisabled());
                autor.fire();
                assertEquals(2, tarjetaMuro.getChildren().size());
                var perfil = new AtomicReference<String>();
                panel.alAbrirPerfil(perfil::set);
                assertFalse(autor.isDisabled());
                autor.fire();
                assertEquals("ana", perfil.get());
                panel.alAbrirPerfil(null);
                assertTrue(autor.isDisabled());

                var primera = new Publicacion("p1", "ana", "Consulta", 2, AmbitoPublicacion.CARRERA, TipoPublicacion.PREGUNTA, "c1", null);
                var segunda = new Publicacion("p2", "autor-ausente", "Pregunta", 3, AmbitoPublicacion.CARRERA, TipoPublicacion.PREGUNTA, "c1", null);
                panel.mostrar(new ArrayList<>(List.of(primera, segunda)), true);
                var tarjetaUno = (VBox) panel.getContenedor().getChildren().get(0);
                var tarjetaDos = (VBox) panel.getContenedor().getChildren().get(1);
                assertEquals("Autor no disponible", ((Label) tarjetaDos.getChildren().getFirst()).getText());
                var ver = (Button) tarjetaDos.getChildren().get(2);
                assertTrue(ver.isDisabled());
                var vista = new AtomicReference<String>();
                panel.alVerComentarios(id -> {
                    vista.set(id);
                    panel.mostrarComentarios(id, new ArrayList<>(List.of(new Comentario("r1", "ana", "Respuesta", 4))));
                });
                ver.fire();
                assertEquals("p2", vista.get());
                assertTrue(((VBox) tarjetaUno.getChildren().get(3)).getChildren().isEmpty());
                var fila = (HBox) ((VBox) tarjetaDos.getChildren().get(3)).getChildren().getFirst();
                assertEquals("Respuesta", ((Label) fila.getChildren().get(1)).getText());
                var editor = (HBox) tarjetaDos.getChildren().get(4);
                var texto = (TextField) editor.getChildren().getFirst();
                var enviar = (Button) editor.getChildren().get(1);
                var enviado = new AtomicReference<List<String>>();
                panel.alComentar((id, valor) -> enviado.set(List.of(id, valor)));
                texto.setText("  ");
                assertTrue(enviar.isDisabled());
                texto.setText(" Mi comentario ");
                enviar.fire();
                assertEquals(List.of("p2", "Mi comentario"), enviado.get());
                assertTrue(texto.getText().isEmpty());
                resultado.complete(null);
            } catch (Throwable error) {
                resultado.completeExceptionally(error);
            }
        });
        resultado.get(10, TimeUnit.SECONDS);
    }
}
