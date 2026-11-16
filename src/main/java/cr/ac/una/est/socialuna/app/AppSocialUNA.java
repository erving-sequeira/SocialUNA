
package cr.ac.una.est.socialuna.app;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.graph.GrafoAmigos;

import cr.ac.una.est.socialuna.persistence.GestorJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioCarrerasJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioSedesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioUsuariosJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioAmistadesJSON;
import cr.ac.una.est.socialuna.persistence.RepositorioPublicacionesJSON;

import cr.ac.una.est.socialuna.service.ServicioUsuarios;
import cr.ac.una.est.socialuna.service.ServicioAmistad;
import cr.ac.una.est.socialuna.service.ServicioPublicacion;
import cr.ac.una.est.socialuna.service.ServicioComunidad;

import cr.ac.una.est.socialuna.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.service.contract.IServicioPublicacion;
import cr.ac.una.est.socialuna.service.contract.IServicioComunidad;

import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import cr.ac.una.est.socialuna.ui.Tema;
import cr.ac.una.est.socialuna.ui.component.PanelPerfil;
import cr.ac.una.est.socialuna.ui.component.PanelPublicaciones;

import cr.ac.una.est.socialuna.ui.controller.ControladorAmigos;
import cr.ac.una.est.socialuna.ui.controller.ControladorLogin;
import cr.ac.una.est.socialuna.ui.controller.ControladorRegistro;
import cr.ac.una.est.socialuna.ui.controller.ControladorFeed;
import cr.ac.una.est.socialuna.ui.controller.ControladorComunidad;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class AppSocialUNA extends Application {

    private final Path directorioDatos;

    private RepositorioUsuariosJSON repositorioUsuarios;
    private RepositorioAmistadesJSON repositorioAmistades;
    private RepositorioPublicacionesJSON repositorioPublicaciones;

    // Deja lista la aplicación para leer y guardar los datos.
    // Utiliza la carpeta datos como ubicación predeterminada.
    public AppSocialUNA() {
        this(Path.of("datos"));
    }

    // Recibe la carpeta donde se van a guardar los archivos JSON.
    // La conserva para utilizarla durante el inicio de la aplicación.
    AppSocialUNA(Path directorioDatos) {
        this.directorioDatos = directorioDatos;
    }

    // Recibe la ventana principal, prepara los servicios y registra los controladores.
    // Finalmente muestra el login para que el usuario pueda ingresar.
    @Override
    public void start(Stage stage) {

        Tema.aplicar();

        stage.setTitle("SocialUNA");
        stage.setMinWidth(520);
        stage.setMinHeight(560);

        ServicioUsuarios usuarios;

        try {
            usuarios = cargarUsuarios();

        } catch (IllegalStateException error) {

            var mensaje = new Label(
                    "No se pudieron cargar los datos. " + error.getMessage()
            );

            mensaje.setWrapText(true);

            var vista = new VBox(12, mensaje);
            vista.setAlignment(Pos.CENTER);
            vista.setStyle("-fx-padding: 30;");

            stage.setScene(new Scene(vista, 800, 600));
            stage.show();

            return;
        }

        // Crea el navegador que permite cambiar entre las diferentes pantallas.
        var navegador = new NavegadorVistas(stage);

        // Crea el servicio de amistades utilizando los repositorios y el grafo.
        IServicioAmistad servicioAmistad = new ServicioAmistad(
                usuarios,
                repositorioUsuarios,
                repositorioAmistades,
                new GrafoAmigos()
        );

        // Crea el servicio encargado de las publicaciones del muro.
        IServicioPublicacion servicioPublicacion = new ServicioPublicacion(
                usuarios,
                servicioAmistad,
                repositorioPublicaciones
        );

        // Crea el servicio que administra las publicaciones de carrera y sede.
        IServicioComunidad servicioComunidad = new ServicioComunidad(
                usuarios,
                repositorioPublicaciones
        );

        // Registra el controlador del inicio de sesión.
        navegador.registrarFabrica(
                ControladorLogin.class,
                () -> new ControladorLogin(usuarios, navegador)
        );

        // Registra el controlador del formulario de registro.
        navegador.registrarFabrica(
                ControladorRegistro.class,
                () -> new ControladorRegistro(usuarios, navegador)
        );

        navegador.registrarFabrica(ControladorAmigos.class,
                () -> new ControladorAmigos(usuarios, servicioAmistad, navegador));
        navegador.registrarFabrica(PanelPerfil.class,
                () -> new PanelPerfil(usuarios, servicioAmistad, navegador));

        navegador.registrarFabrica(ControladorComunidad.class,
                () -> new ControladorComunidad(usuarios, servicioComunidad, navegador, new PanelPublicaciones(usuarios)));

        navegador.registrarFabrica(
                ControladorFeed.class,
                () -> new ControladorFeed(
                        servicioPublicacion,
                        navegador,
                        new PanelPublicaciones(usuarios)
                )
        );

        // Abre la pantalla inicial y muestra la ventana.
        navegador.mostrarLogin();
        stage.show();
    }

    //Abre los repositorios, prepara los catálogos oficiales y devuelve el servicio de usuarios.
    private ServicioUsuarios cargarUsuarios() {

        var gestor = new GestorJSON(directorioDatos);

        repositorioUsuarios = new RepositorioUsuariosJSON(gestor);

        var carreras = new RepositorioCarrerasJSON(gestor);
        var sedes = new RepositorioSedesJSON(gestor);

        repositorioAmistades = new RepositorioAmistadesJSON(gestor);
        repositorioAmistades.cargar();

        repositorioPublicaciones = new RepositorioPublicacionesJSON(gestor);
        repositorioPublicaciones.cargar();

        prepararCarreras(gestor, carreras);
        if (Files.notExists(directorioDatos.resolve("sedes.json"))) {
            sedes.guardarSedes(List.of(gestor.leerCatalogoBase("sedes.json", SedeCampus[].class)));
        }

        return new ServicioUsuarios(
                repositorioUsuarios,
                sedes,
                carreras
        );
    }

    //Recibe el gestor y el repositorio. Crea el catálogo o completa las sedes de las carreras antiguas.
    private void prepararCarreras(GestorJSON gestor, RepositorioCarrerasJSON carreras) {
        if (Files.notExists(directorioDatos.resolve("carreras.json"))) {
            carreras.guardarCarreras(List.of(gestor.leerCatalogoBase("carreras.json", Carrera[].class)));
            return;
        }

        var actuales = carreras.listarCarreras();
        if (actuales.stream().allMatch(Carrera::tieneSedesConfiguradas)) return;

        var base = List.of(gestor.leerCatalogoBase("carreras.json", Carrera[].class)).stream()
                .collect(Collectors.toMap(Carrera::getId, Carrera::getSedeIds));
        var actualizadas = actuales.stream().map(carrera -> {
            var sedes = base.get(carrera.getId());
            if (carrera.tieneSedesConfiguradas() || sedes == null) return carrera;
            return new Carrera(carrera.getId(), carrera.getNombre(), carrera.isActiva(), sedes);
        }).toList();
        if (!actualizadas.equals(actuales)) carreras.guardarCarreras(actualizadas);
    }
}
