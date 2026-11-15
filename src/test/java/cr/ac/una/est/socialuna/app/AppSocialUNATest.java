package cr.ac.una.est.socialuna.app;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import cr.ac.una.est.socialuna.model.entity.*;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.component.PanelPublicaciones;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import cr.ac.una.est.socialuna.persistence.GestorJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioUsuariosJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioCarrerasJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioSedesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioPublicacionesJSON;
import cr.ac.una.est.socialuna.service.ServicioUsuarios;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.kordamp.ikonli.javafx.FontIcon;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfSystemProperty(named="pruebas.javafx",matches="true")
class AppSocialUNATest {
    //Enciende JavaFX una vez para poder probar las pantallas reales.
    @BeforeAll static void iniciarJavaFx() throws Exception {
        CompletableFuture<Void> inicio = new CompletableFuture<>();
        Platform.startup(() -> { Platform.setImplicitExit(false); inicio.complete(null); });
        inicio.get(10,TimeUnit.SECONDS);
    }

    @TempDir Path datos;

    //Prueba que SocialUNA arranque mostrando el login.
    @Test void iniciaLaVentanaSocialUnaConLogin() throws Exception {
        CompletableFuture<Void> resultado = new CompletableFuture<>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                stage.setOpacity(0);
                new AppSocialUNA(datos).start(stage);
                assertTrue(stage.isShowing()); assertEquals("SocialUNA",stage.getTitle());
                assertEquals(800,stage.getScene().getWidth(),1); assertEquals(600,stage.getScene().getHeight(),1);
                assertEquals("vistaLogin", stage.getScene().getRoot().getId());
                resultado.complete(null);
            } catch(Throwable error) { resultado.completeExceptionally(error); }
            finally { stage.close(); }
        });
        resultado.get(10,TimeUnit.SECONDS);
    }

    //Prueba el registro, el login y la salida usando los botones de las pantallas reales.
    @Test void registraUnaCuentaIniciaSesionYCierraSesionDesdeLasVistasReales() throws Exception {
        var resultado = new CompletableFuture<Void>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                stage.setOpacity(0);
                new AppSocialUNA(datos).start(stage);
                var escena = stage.getScene();
                ((Hyperlink) control(escena, "#enlaceRegistro")).fire();
                assertSame(escena, stage.getScene());
                assertEquals("vistaRegistro", escena.getRoot().getId());
                ((Hyperlink) control(escena, "#enlaceVolver")).fire();
                assertEquals("vistaLogin", escena.getRoot().getId());
                ((Hyperlink) control(escena, "#enlaceRegistro")).fire();
                ((TextField) control(escena, "#nombre")).setText("Ana María");
                ((TextField) control(escena, "#nombreUsuario")).setText(" ANA ");
                ((TextField) control(escena, "#contrasena")).setText("prueba");
                ((Button) control(escena, "#btnRegistrar")).fire();
                assertTrue(((Label) control(escena, "#mensaje")).getText().contains("carrera y tu sede"));
                assertFalse(Files.exists(datos.resolve("usuarios.json")));
                var carreras = (ComboBox<?>) control(escena, "#carrera");
                var sedes = (ComboBox<?>) control(escena, "#sede");
                assertFalse(carreras.getItems().isEmpty());
                assertFalse(sedes.getItems().isEmpty());
                carreras.getSelectionModel().selectFirst();
                sedes.getSelectionModel().selectFirst();
                ((Button) control(escena, "#btnRegistrar")).fire();
                assertEquals("vistaLogin", escena.getRoot().getId());
                var usuario = new RepositorioUsuariosJSON(new GestorJSON(datos)).buscarUsuarioPorNombreUsuario("ana");
                assertNotNull(usuario);
                assertNotNull(usuario.getCarreraId());
                assertNotNull(usuario.getSedeId());

                ((TextField) control(escena, "#nombreUsuario")).setText("ANA");
                var clave = (TextField) control(escena, "#contrasena");
                clave.setText("incorrecta");
                ((Button) control(escena, "#btnIngresar")).fire();
                assertEquals("vistaLogin", escena.getRoot().getId());
                assertTrue(((Label) control(escena, "#mensaje")).getText().contains("incorrectos"));
                assertEquals("", clave.getText());
                clave.setText("prueba");
                ((Button) control(escena, "#btnIngresar")).fire();
                assertNotNull(control(escena, "#vistaFeed"));
                ((Button) control(escena, "#btnMiPerfil")).fire();
                assertNotNull(control(escena, "#btnCerrarSesion"));
                ((Button) control(escena, "#btnCerrarSesion")).fire();
                assertEquals("vistaLogin", escena.getRoot().getId());
                ((TextField) control(escena, "#nombreUsuario")).setText("ana");
                ((TextField) control(escena, "#contrasena")).setText("prueba");
                ((Button) control(escena, "#btnIngresar")).fire();
                assertNotNull(control(escena, "#vistaFeed"));
                ((Button) control(escena, "#btnMiPerfil")).fire();
                assertNotNull(control(escena, "#btnCerrarSesion"));
                resultado.complete(null);
            } catch (Throwable error) {
                resultado.completeExceptionally(error);
            } finally {
                stage.close();
            }
        });
        resultado.get(15, TimeUnit.SECONDS);
    }

    //Prueba que el botón de tema alterne entre el estilo claro y el oscuro.
    @Test void botonDeTemaAlternaElEstiloDeLaApp() throws Exception {
        var resultado = new CompletableFuture<Void>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                stage.setOpacity(0);
                new AppSocialUNA(datos).start(stage);
                var escena = stage.getScene();
                var inicial = Application.getUserAgentStylesheet();
                var icono = (FontIcon) control(escena, ".ikonli-font-icon");
                assertNotNull(icono);
                assertNotEquals(0, icono.getIconCode());
                ((Button) control(escena, "#btnTema")).fire();
                var alternado = Application.getUserAgentStylesheet();
                assertNotEquals(inicial, alternado);
                ((Button) control(escena, "#btnTema")).fire();
                assertEquals(inicial, Application.getUserAgentStylesheet());
                resultado.complete(null);
            } catch (Throwable error) {
                resultado.completeExceptionally(error);
            } finally {
                stage.close();
            }
        });
        resultado.get(10, TimeUnit.SECONDS);
    }

    //Prueba la búsqueda, la amistad y los datos del perfil desde el muro conectado al JSON.
    @Test void abrePerfilesDesdeLaBusquedaYElMuro() throws Exception {
        var resultado = new CompletableFuture<Void>();
        Platform.runLater(() -> {
            Stage stage = new Stage();
            try {
                stage.setOpacity(0);
                new AppSocialUNA(datos).start(stage);
                var gestor = new GestorJSON(datos);
                var repo = new RepositorioUsuariosJSON(gestor);
                var usuarios = new ServicioUsuarios(repo, new RepositorioSedesJSON(gestor),
                        new RepositorioCarrerasJSON(gestor));
                var ana = usuarios.registrar("Ana", "ana", "prueba", "informatica", "central");
                var beto = usuarios.registrar("Beto", "beto", "prueba", "informatica", "central");
                var publicaciones = new RepositorioPublicacionesJSON(gestor);
                publicaciones.cargar();
                publicaciones.guardar(new Publicacion("beto-muro", beto.getId(), "Hola desde el muro",
                        100, AmbitoPublicacion.MURO, TipoPublicacion.GENERAL, null, null));
                publicaciones.guardar(new Publicacion("beto-carrera", beto.getId(), "Desde la SIUA",
                        101, AmbitoPublicacion.CARRERA, TipoPublicacion.GENERAL, "informatica", "siua"));

                new AppSocialUNA(datos).start(stage);
                var escena = stage.getScene();
                ((TextField) control(escena, "#nombreUsuario")).setText("ana");
                ((TextField) control(escena, "#contrasena")).setText("prueba");
                ((Button) control(escena, "#btnIngresar")).fire();
                String seleccionado = "opcion-seleccionada";
                var menu = control(escena, "#menuInferior");
                assertNotNull(menu);
                var tarjetas = (VBox) control(escena, "#listaTarjetas");
                assertInstanceOf(Label.class, tarjetas.getChildren().getFirst());

                ((Button) control(escena, "#btnBuscarEstudiantes")).fire();
                var busqueda = control(escena, "#vistaBusqueda");
                assertNotNull(busqueda);
                assertSame(menu, control(escena, "#menuInferior"));
                ((TextField) control(escena, "#campoBusqueda")).setText(" BET ");
                ((Button) control(escena, "#btnBuscar")).fire();
                var resultados = (VBox) control(escena, "#contenedorResultados");
                var fila = (HBox) resultados.getChildren().getFirst();
                ((Button) fila.getChildren().getLast()).fire();
                assertNotNull(control(escena, "#vistaPerfil"));
                assertEquals("Beto", ((Label) control(escena, ".marca")).getText());
                assertTrue(control(escena, "#btnBuscarEstudiantes").getStyleClass().contains(seleccionado));
                assertFalse(control(escena, "#btnMiPerfil").getStyleClass().contains(seleccionado));

                var amistad = (Button) control(escena, "#btnAmistad");
                assertNull(control(escena, "#btnCerrarSesion"));
                var total = (Label) control(escena, "#cantidadAmigos");
                assertEquals("Amigos · 0", total.getText());
                amistad.fire();
                assertEquals("Amigos · 1", total.getText());
                assertSame(menu, control(escena, "#menuInferior"));
                ((Button) control(escena, "#btnBuscarEstudiantes")).fire();
                assertSame(busqueda, control(escena, "#vistaBusqueda"));
                var amigos = (VBox) control(escena, "#contenedorAmigos");
                assertInstanceOf(HBox.class, amigos.getChildren().getFirst());
                assertNull(control(escena, "#volverBusqueda"));
                ((Button) control(escena, "#btnMuro")).fire();
                assertNotNull(control(escena, "#vistaFeed"));

                var tarjeta = (VBox) tarjetas.getChildren().getFirst();
                var autor = (Hyperlink) ((HBox) tarjeta.getChildren().getFirst()).getChildren().getFirst();
                assertEquals("Beto", autor.getText());
                autor.fire();
                assertNotNull(control(escena, "#vistaPerfil"));
                assertEquals("Beto", ((Label) control(escena, ".marca")).getText());
                assertTrue(control(escena, "#btnMuro").getStyleClass().contains(seleccionado));
                assertFalse(control(escena, "#btnMiPerfil").getStyleClass().contains(seleccionado));
                ((Button) control(escena, "#btnAmistad")).fire();
                assertEquals("Amigos · 0", ((Label) control(escena, "#cantidadAmigos")).getText());
                ((Button) control(escena, "#btnMuro")).fire();
                assertNotNull(control(escena, "#vistaFeed"));
                assertInstanceOf(Label.class, tarjetas.getChildren().getFirst());

                var formulario = control(escena, "#formularioPublicacion");
                assertFalse(formulario.isVisible());
                assertFalse(formulario.isManaged());
                ((Button) control(escena, "#btnNuevaPublicacion")).fire();
                assertTrue(formulario.isVisible());
                ((TextArea) control(escena, "#textoPublicacion")).setText("Hola desde mi cuenta");
                ((Button) control(escena, "#btnPublicar")).fire();
                assertFalse(formulario.isVisible());
                var normal = (VBox) tarjetas.getChildren().getFirst();
                assertTrue(normal.getStyleClass().contains("tarjeta-publicacion"));
                assertEquals("Publicación", ((Label) normal.lookup(".tipo-publicacion")).getText());

                var pestanas = (TabPane) control(escena, "#pestanas");
                for (var tab : pestanas.getTabs()) {
                    pestanas.getSelectionModel().select(tab);
                    ((Button) control(escena, "#btnNuevaPublicacion")).fire();
                    ((RadioButton) control(escena, "#tipoPregunta")).fire();
                    ((TextArea) control(escena, "#textoPublicacion")).setText("¿Una pregunta en " + tab.getText() + "?");
                    ((Button) control(escena, "#btnPublicar")).fire();
                    assertFalse(formulario.isVisible());
                    var listaActual = (VBox) control(escena, "#listaTarjetas");
                    var pregunta = (VBox) listaActual.getChildren().getFirst();
                    assertTrue(pregunta.getStyleClass().contains("tarjeta-pregunta"));
                    assertEquals("Pregunta", ((Label) pregunta.lookup(".tipo-pregunta")).getText());
                    if ("tabMuro".equals(tab.getId())) {
                        assertEquals(2, pregunta.getChildren().size());
                    } else {
                        comprobarComentarios(pregunta);
                    }
                }
                publicaciones.cargar();
                for (var ambito : AmbitoPublicacion.values()) {
                    var pregunta = publicaciones.listar().stream()
                            .filter(publicacion -> publicacion.getAmbito() == ambito
                                    && publicacion.getTipo() == TipoPublicacion.PREGUNTA)
                            .findFirst().orElseThrow();
                    if (ambito != AmbitoPublicacion.MURO) {
                        assertEquals("Respuesta de prueba", pregunta.getComentarios().getFirst().getTexto());
                    }
                }

                pestanas.getSelectionModel().select(1);
                var filtro = (MenuButton) control(escena, "#filtroSede");
                filtro.getItems().getLast().fire();
                var filtradas = (VBox) control(escena, "#listaTarjetas");
                assertEquals(1, filtradas.getChildren().size());
                var postFiltrado = (VBox) filtradas.getChildren().getFirst();
                assertEquals("Desde la SIUA", ((Label) postFiltrado.getChildren().get(1)).getText());
                pestanas.getSelectionModel().select(2);
                assertFalse(control(escena, "#filtroSede").isVisible()
                        && control(escena, "#filtroSede").getParent().isVisible());
                pestanas.getSelectionModel().select(1);
                assertSame(filtro, control(escena, "#filtroSede"));
                assertEquals(1, filtradas.getChildren().size());
                filtro.getItems().getFirst().fire();
                assertEquals(2, filtradas.getChildren().size());

                ((Button) control(escena, "#btnMiPerfil")).fire();
                assertNull(control(escena, "#btnAmistad"));
                assertEquals("Ana", ((Label) control(escena, ".marca")).getText());
                assertTrue(control(escena, "#btnMiPerfil").getStyleClass().contains(seleccionado));
                assertSame(menu, control(escena, "#menuInferior"));
                var editor = control(escena, "#formularioPerfil");
                assertFalse(editor.isVisible());
                assertFalse(editor.isManaged());
                ((Button) control(escena, "#btnEditarPerfil")).fire();
                assertTrue(editor.isVisible());
                ((ChoiceBox<?>) control(escena, "#editarCarrera")).getSelectionModel().selectLast();
                ((Button) control(escena, "#btnCancelarPerfil")).fire();
                assertFalse(editor.isVisible());
                ((Button) control(escena, "#btnEditarPerfil")).fire();
                var carreras = (ChoiceBox<?>) control(escena, "#editarCarrera");
                assertEquals("informatica", ((Carrera) carreras.getValue()).getId());
                carreras.getSelectionModel().selectLast();
                ((ChoiceBox<?>) control(escena, "#editarSede")).getSelectionModel().selectLast();
                ((Button) control(escena, "#btnGuardarPerfil")).fire();
                assertFalse(editor.isVisible());
                var guardado = new RepositorioUsuariosJSON(gestor).buscarUsuarioPorId(ana.getId());
                assertEquals("administracion", guardado.getCarreraId());
                assertEquals("siua", guardado.getSedeId());
                ((Button) control(escena, "#btnCerrarSesion")).fire();
                assertEquals("vistaLogin", escena.getRoot().getId());
                resultado.complete(null);
            } catch (Throwable error) {
                resultado.completeExceptionally(error);
            } finally {
                stage.close();
            }
        });
        resultado.get(15, TimeUnit.SECONDS);
    }

    //Recibe una tarjeta de comunidad y prueba que su comentario se vea sin recargar la lista.
    private static void comprobarComentarios(VBox tarjeta) {
        var ver = (Button) tarjeta.getChildren().get(2);
        assertFalse(ver.isDisabled());
        ver.fire();
        var comentarios = (VBox) tarjeta.getChildren().get(3);
        assertEquals("Todavía no hay comentarios.", ((Label) comentarios.getChildren().getFirst()).getText());
        var editor = (HBox) tarjeta.getChildren().get(4);
        var campo = (TextField) editor.getChildren().getFirst();
        campo.setText("Respuesta de prueba");
        ((Button) editor.getChildren().getLast()).fire();
        assertTrue(campo.getText().isEmpty());
        var fila = (HBox) comentarios.getChildren().getFirst();
        assertEquals("Respuesta de prueba", ((Label) fila.getChildren().getLast()).getText());
    }

    //Recibe la escena y un selector. Prepara el contenido de ScrollPane y devuelve el nodo que coincide.
    private static Node control(Scene escena, String selector) {
        escena.getRoot().applyCss();
        escena.getRoot().layout();
        return escena.lookup(selector);
    }

    //Prueba que el panel muestre los controles correctos y mande cada acción al post que toca.
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
                var autor = (Hyperlink) ((HBox) tarjetaMuro.getChildren().getFirst()).getChildren().getFirst();
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
                assertEquals("Autor no disponible", ((Label) ((HBox) tarjetaDos.getChildren().getFirst()).getChildren().getFirst()).getText());
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
                panel.alComentar((id, valor) -> false);
                texto.setText("Borrador pendiente");
                enviar.fire();
                assertEquals("Borrador pendiente", texto.getText());
                panel.alComentar((id, valor) -> {
                    enviado.set(List.of(id, valor));
                    return true;
                });
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
