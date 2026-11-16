package cr.ac.una.est.socialuna.ui.component;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
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
        contenedor.setId("vistaPerfil");
        contenedor.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
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

        var nombre = new Label(perfil.getNombre());
        nombre.setWrapText(true);
        nombre.getStyleClass().add("marca");
        var alias = new Label("@" + perfil.getNombreUsuario());

        var carrera = new Label();
        carrera.setWrapText(true);
        var sede = new Label();
        sede.setWrapText(true);
        mostrarDatosAcademicos(perfil, carrera, sede);

        var cantidad = new Label();
        cantidad.setId("cantidadAmigos");
        cantidad.getStyleClass().add("contador-amigos");
        mostrarTotal(perfilId, cantidad);

        var tituloDatos = new Label("Datos académicos");
        tituloDatos.getStyleClass().add("titulo");
        var datos = new VBox(10, tituloDatos, carrera, sede);
        datos.getStyleClass().add("panel-social");

        var acciones = new VBox(12);
        boolean propio = actual.getId().equals(perfil.getId());
        if (propio) {
            mostrarAccionesPropias(acciones, datos, carrera, sede);
        } else {
            mostrarAccionAmistad(acciones, actual.getId(), perfilId, cantidad);
        }

        mensaje.setText("");
        var titulo = new Label(propio ? "Mi perfil" : "Perfil de estudiante");
        titulo.getStyleClass().add("titulo");
        var identidad = new VBox(6, nombre, alias, cantidad);
        identidad.getStyleClass().add("panel-social");
        var cuerpo = new VBox(14, titulo, identidad, datos, acciones, mensaje);
        cuerpo.getStyleClass().add("cuerpo-social");
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

    //Recibe el área de datos y sus etiquetas. Arma el editor con sedes según la carrera elegida.
    private void mostrarAccionesPropias(VBox acciones, VBox datos, Label carrera, Label sede) {
        ChoiceBox<Carrera> carreras = new ChoiceBox<>();
        carreras.setId("editarCarrera");
        carreras.setAccessibleText("Carrera");
        usarNombre(carreras, Carrera::getNombre);
        carreras.getItems().setAll(usuarios.listarCarreras());

        ChoiceBox<SedeCampus> sedes = new ChoiceBox<>();
        sedes.setId("editarSede");
        sedes.setAccessibleText("Sede");
        usarNombre(sedes, SedeCampus::getNombre);
        sedes.disableProperty().bind(Bindings.isEmpty(sedes.getItems()));
        carreras.valueProperty().addListener((propiedad, anterior, seleccion) -> {
            sedes.setValue(null);
            sedes.getItems().setAll(usuarios.listarSedesPorCarrera(seleccion == null ? null : seleccion.getId()));
            mensaje.getStyleClass().setAll("label", "error");
            mensaje.setText(seleccion != null && sedes.getItems().isEmpty()
                    ? "Esta carrera no tiene sedes activas." : "");
        });
        var editar = new Button("Editar datos");
        editar.setId("btnEditarPerfil");
        editar.getStyleClass().add("accent");
        var cancelar = new Button("Cancelar");
        cancelar.setId("btnCancelarPerfil");
        var guardar = new Button("Guardar cambios");
        guardar.setId("btnGuardarPerfil");
        guardar.getStyleClass().add("accent");
        var titulo = new Label("Editar datos académicos");
        titulo.getStyleClass().add("titulo");
        var formulario = new VBox(10, titulo, new Label("Carrera"), carreras, new Label("Sede"), sedes,
                new FlowPane(10, 8, cancelar, guardar));
        formulario.getStyleClass().add("panel-social");
        formulario.setId("formularioPerfil");
        formulario.setVisible(false);
        formulario.managedProperty().bind(formulario.visibleProperty());
        datos.visibleProperty().bind(formulario.visibleProperty().not());
        datos.managedProperty().bind(datos.visibleProperty());
        editar.setOnAction(evento -> {
            Usuario perfil = usuarios.getUsuarioActual();
            mensaje.setText("");
            carreras.setValue(carreras.getItems().stream()
                    .filter(valor -> valor.getId().equals(perfil.getCarreraId())).findFirst().orElse(null));
            sedes.setValue(sedes.getItems().stream()
                    .filter(valor -> valor.getId().equals(perfil.getSedeId())).findFirst().orElse(null));
            if (carreras.getValue() != null && sedes.getItems().isEmpty()) {
                mensaje.getStyleClass().setAll("label", "error");
                mensaje.setText("Esta carrera no tiene sedes activas.");
            }
            formulario.setVisible(true);
            carreras.requestFocus();
        });
        cancelar.setOnAction(evento -> {
            formulario.setVisible(false);
            mensaje.setText("");
            editar.requestFocus();
        });
        guardar.disableProperty().bind(carreras.valueProperty().isNull().or(sedes.valueProperty().isNull()));
        guardar.setOnAction(evento -> {
            try {
                Usuario actualizado = usuarios.actualizarDatosAcademicos(carreras.getValue().getId(),
                        sedes.getValue().getId());
                mostrarDatosAcademicos(actualizado, carrera, sede);
                formulario.setVisible(false);
                editar.requestFocus();
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
        var botones = new FlowPane(10, 8, editar, salir);
        botones.visibleProperty().bind(formulario.visibleProperty().not());
        botones.managedProperty().bind(botones.visibleProperty());
        acciones.getChildren().addAll(botones, formulario);
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
