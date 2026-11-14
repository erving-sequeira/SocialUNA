package cr.ac.una.est.socialuna.ui.component;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.function.Function;

public class PanelPerfil {
    private final IServicioUsuarios usuarios;
    private final IServicioAmistad amistades;
    private final NavegadorVistas navegador;
    private final ScrollPane contenedor = new ScrollPane();
    private final Label mensaje = new Label();

    //Recibe los servicios y el navegador para armar el perfil propio o el de otra persona.
    public PanelPerfil(IServicioUsuarios usuarios, IServicioAmistad amistades, NavegadorVistas navegador) {
        this.usuarios = usuarios;
        this.amistades = amistades;
        this.navegador = navegador;
        contenedor.setFitToWidth(true);
        contenedor.getStyleClass().add("acceso");
        contenedor.getStylesheets().add(getClass().getResource("/cr/ac/una/est/socialuna/ui/css/acceso.css").toExternalForm());
        mensaje.setWrapText(true);
        mensaje.visibleProperty().bind(mensaje.textProperty().isNotEmpty());
        mensaje.managedProperty().bind(mensaje.visibleProperty());
    }

    //Recibe el ID y muestra el perfil con los controles que correspondan. Devuelve la pantalla armada.
    public ScrollPane mostrarPerfil(String perfilId) {
        Usuario actual = usuarios.getUsuarioActual();
        if (actual == null) throw new IllegalStateException("Inicia sesión para ver perfiles.");
        Usuario perfil = usuarios.obtenerPerfil(perfilId);
        if (perfil == null) throw new IllegalArgumentException("Ese perfil ya no está disponible.");

        var volver = new Hyperlink("Volver");
        volver.setId("volverPerfil");
        volver.setOnAction(evento -> navegador.volver());

        var nombre = new Label(perfil.getNombre());
        nombre.setWrapText(true);
        nombre.getStyleClass().add("marca");
        var alias = new Label("@" + perfil.getNombreUsuario());
        alias.getStyleClass().add("titulo");

        var carrera = new Label();
        carrera.setWrapText(true);
        var sede = new Label();
        sede.setWrapText(true);
        mostrarDatosAcademicos(perfil, carrera, sede);

        var cantidad = new Label();
        cantidad.setId("cantidadAmigos");
        cantidad.getStyleClass().add("contador-amigos");
        mostrarTotal(perfilId, cantidad);

        var acciones = new VBox(12);
        if (actual.getId().equals(perfil.getId())) {
            mostrarAccionesPropias(acciones, perfil, carrera, sede);
        } else {
            mostrarAccionAmistad(acciones, actual.getId(), perfilId, cantidad);
        }

        mensaje.setText("");
        var tarjeta = new VBox(10, nombre, alias, carrera, sede, cantidad, acciones, mensaje);
        tarjeta.getStyleClass().add("tarjeta");
        var cuerpo = new VBox(14, volver, tarjeta);
        cuerpo.getStyleClass().add("marco");
        contenedor.setContent(cuerpo);
        return contenedor;
    }

    //Recibe el perfil y pone los nombres de carrera y sede en sus etiquetas.
    private void mostrarDatosAcademicos(Usuario perfil, Label carrera, Label sede) {
        String nombreCarrera = usuarios.listarCarreras().stream()
                .filter(valor -> valor.getId().equals(perfil.getCarreraId()))
                .map(Carrera::getNombre).findFirst().orElse(perfil.getCarreraId());
        String nombreSede = usuarios.listarSedes().stream()
                .filter(valor -> valor.getId().equals(perfil.getSedeId()))
                .map(SedeCampus::getNombre).findFirst().orElse(perfil.getSedeId());
        carrera.setText("Carrera · " + (nombreCarrera == null ? "Sin seleccionar" : nombreCarrera));
        sede.setText("Sede · " + (nombreSede == null ? "Sin seleccionar" : nombreSede));
    }

    //Recibe el perfil y su contador. Pone el total de amigos que devuelve el servicio.
    private void mostrarTotal(String perfilId, Label contador) {
        contador.setText("Amigos · " + amistades.contarAmigosDe(perfilId));
    }

    //Pone el botón de amistad y actualiza el botón y el total después de guardar.
    private void mostrarAccionAmistad(VBox acciones, String propioId, String perfilId, Label cantidad) {
        var boton = new Button();
        boton.setId("btnAmistad");
        boton.setText(amistades.sonAmigos(propioId, perfilId) ? "Quitar amistad" : "Agregar amigo");
        boton.setOnAction(evento -> {
            mensaje.setText("");
            try {
                if (amistades.sonAmigos(propioId, perfilId)) amistades.eliminarAmigo(perfilId);
                else amistades.agregarAmigo(perfilId);
                mostrarTotal(perfilId, cantidad);
                boton.setText(amistades.sonAmigos(propioId, perfilId) ? "Quitar amistad" : "Agregar amigo");
            } catch (IllegalArgumentException | IllegalStateException error) {
                mensaje.getStyleClass().setAll("label", "error");
                mensaje.setText(error.getMessage());
            }
        });
        acciones.getChildren().add(boton);
    }

    //Recibe el perfil y sus etiquetas. Agrega los controles para editar datos y cerrar sesión.
    private void mostrarAccionesPropias(VBox acciones, Usuario perfil, Label carrera, Label sede) {
        ChoiceBox<Carrera> carreras = new ChoiceBox<>();
        carreras.setId("editarCarrera");
        carreras.setAccessibleText("Carrera");
        usarNombre(carreras, Carrera::getNombre);
        carreras.getItems().setAll(usuarios.listarCarreras());
        carreras.setValue(carreras.getItems().stream()
                .filter(valor -> valor.getId().equals(perfil.getCarreraId())).findFirst().orElse(null));

        ChoiceBox<SedeCampus> sedes = new ChoiceBox<>();
        sedes.setId("editarSede");
        sedes.setAccessibleText("Sede");
        usarNombre(sedes, SedeCampus::getNombre);
        sedes.getItems().setAll(usuarios.listarSedes());
        sedes.setValue(sedes.getItems().stream()
                .filter(valor -> valor.getId().equals(perfil.getSedeId())).findFirst().orElse(null));

        var guardar = new Button("Guardar cambios");
        guardar.setId("btnGuardarPerfil");
        guardar.disableProperty().bind(carreras.valueProperty().isNull().or(sedes.valueProperty().isNull()));
        guardar.setOnAction(evento -> {
            try {
                Usuario actualizado = usuarios.actualizarDatosAcademicos(carreras.getValue().getId(),
                        sedes.getValue().getId());
                mostrarDatosAcademicos(actualizado, carrera, sede);
                mensaje.getStyleClass().setAll("label");
                mensaje.setText("Cambios guardados.");
            } catch (IllegalArgumentException | IllegalStateException error) {
                mensaje.getStyleClass().setAll("label", "error");
                mensaje.setText(error.getMessage());
            }
        });
        var salir = new Button("Cerrar sesión");
        salir.setId("btnCerrarSesion");
        salir.setOnAction(evento -> {
            usuarios.cerrarSesion();
            navegador.mostrarLogin();
        });
        acciones.getChildren().addAll(new FlowPane(10, 8, carreras, sedes), new FlowPane(10, 8, guardar, salir));
    }

    //Recibe el selector y la función que saca el nombre. Hace que el selector muestre ese nombre.
    private static <T> void usarNombre(ChoiceBox<T> selector, Function<T, String> nombre) {
        selector.setConverter(new StringConverter<>() {
            //Recibe una opción y devuelve el nombre que aparece en pantalla.
            @Override public String toString(T valor) {
                return valor == null ? "" : nombre.apply(valor);
            }

            //Recibe texto y devuelve null porque el selector no admite escribir opciones.
            @Override public T fromString(String texto) {
                return null;
            }
        });
    }
}
