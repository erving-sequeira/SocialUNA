package cr.ac.una.est.socialuna.ui.component;

import cr.ac.una.est.socialuna.model.entity.Carrera;
import cr.ac.una.est.socialuna.model.entity.SedeCampus;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.contract.IServicioAmistad;
import cr.ac.una.est.socialuna.service.contract.IServicioPublicacion;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.function.Function;

public class PanelPerfil {
    private final IServicioUsuarios usuarios;
    private final IServicioAmistad amistades;
    private final IServicioPublicacion publicaciones;
    private final NavegadorVistas navegador;
    private final PanelPublicaciones panelPublicaciones;
    private final ScrollPane contenedor = new ScrollPane();
    private final Label mensaje = new Label();
    private String perfilIdActual;

    //Recibe los servicios y el navegador para armar el perfil propio o el de otra persona.
    public PanelPerfil(IServicioUsuarios usuarios, IServicioAmistad amistades,
                       NavegadorVistas navegador, IServicioPublicacion publicaciones) {
        this.usuarios = usuarios;
        this.amistades = amistades;
        this.navegador = navegador;
        this.publicaciones = publicaciones;
        this.panelPublicaciones = new PanelPublicaciones(usuarios);
        contenedor.setFitToWidth(true);
        contenedor.setId("vistaPerfil");
        contenedor.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        mensaje.setWrapText(true);
        mensaje.visibleProperty().bind(mensaje.textProperty().isNotEmpty());
        mensaje.managedProperty().bind(mensaje.visibleProperty());

        panelPublicaciones.alAbrirPerfil(this::abrirPerfil);
        panelPublicaciones.alComentar(this::comentar);
        panelPublicaciones.alResponder(this::responder);
        panelPublicaciones.alEliminarComentario(this::eliminarComentario);
        panelPublicaciones.alVerComentarios(this::mostrarComentarios);
        panelPublicaciones.alEliminarPublicacion(this::eliminarPublicacion);
        panelPublicaciones.alReaccionar(this::reaccionar);
    }

    //Recibe el ID y muestra el perfil con los controles que correspondan. Devuelve la pantalla armada.
    public ScrollPane mostrarPerfil(String perfilId) {
        Usuario actual = usuarios.getUsuarioActual();
        if (actual == null) throw new IllegalStateException("Inicia sesión para ver perfiles.");
        Usuario perfil = usuarios.obtenerPerfil(perfilId);
        if (perfil == null) throw new IllegalArgumentException("Ese perfil ya no está disponible.");
        perfilIdActual = perfilId;

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

        var identidadTexto = new VBox(6, nombre, alias, cantidad);
        var identidad = new HBox(12, Avatar.crear(perfil, 48), identidadTexto);
        identidad.setAlignment(Pos.CENTER_LEFT);
        identidad.getStyleClass().add("panel-social");

        var tituloPublicaciones = new Label("Publicaciones");
        tituloPublicaciones.getStyleClass().add("titulo");
        panelPublicaciones.mostrar(publicaciones.obtenerPublicacionesDe(perfilId), true);
        var seccionPublicaciones = new VBox(10, tituloPublicaciones, panelPublicaciones.getContenedor());

        var cuerpo = new VBox(14, titulo, identidad, datos, acciones, mensaje, seccionPublicaciones);
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

    //Recibe el ID y abre ese perfil desde una publicación.
    private void abrirPerfil(String perfilId) {
        try {
            navegador.mostrarPerfil(perfilId);
        } catch (IllegalArgumentException | IllegalStateException error) {
            mostrarError(error.getMessage());
        }
    }

    //Recibe el post y el texto. Devuelve true si se guardó el comentario.
    private boolean comentar(String publicacionId, String texto) {
        try {
            publicaciones.comentar(publicacionId, texto);
        } catch (IllegalArgumentException | IllegalStateException error) {
            mostrarError(error.getMessage());
            return false;
        }
        mostrarComentarios(publicacionId);
        return true;
    }

    //Recibe el post, el comentario padre y el texto. Devuelve true si se guardó la respuesta.
    private boolean responder(String publicacionId, String comentarioPadreId, String texto) {
        try {
            publicaciones.responder(publicacionId, comentarioPadreId, texto);
        } catch (IllegalArgumentException | IllegalStateException error) {
            mostrarError(error.getMessage());
            return false;
        }
        mostrarComentarios(publicacionId);
        return true;
    }

    //Recibe el post y el comentario propio. Devuelve true si se borró el comentario.
    private boolean eliminarComentario(String publicacionId, String comentarioId) {
        if (!confirmar("¿Eliminar este comentario? Se borrarán también sus respuestas.",
                "Eliminar comentario")) {
            return false;
        }
        try {
            publicaciones.eliminarComentario(publicacionId, comentarioId);
        } catch (IllegalArgumentException | IllegalStateException error) {
            mostrarError(error.getMessage());
            return false;
        }
        mostrarComentarios(publicacionId);
        return true;
    }

    //Recibe el ID del post y muestra sus comentarios en la misma tarjeta.
    private void mostrarComentarios(String publicacionId) {
        try {
            panelPublicaciones.mostrarComentarios(publicacionId,
                    publicaciones.obtenerComentarios(publicacionId));
            limpiarMensaje();
        } catch (IllegalArgumentException | IllegalStateException error) {
            mostrarError(error.getMessage());
        }
    }

    //Recibe el ID del post. Pide confirmación, lo elimina y vuelve a pintar el perfil.
    private boolean eliminarPublicacion(String publicacionId) {
        if (!confirmarEliminacion()) return false;
        try {
            publicaciones.eliminarPublicacion(publicacionId);
            mostrarPerfil(perfilIdActual);
            return true;
        } catch (IllegalArgumentException | IllegalStateException error) {
            mostrarError(error.getMessage());
            return false;
        }
    }

    //Recibe el ID del post y alterna el me gusta del usuario en sesión.
    private void reaccionar(String publicacionId) {
        try {
            var publicacion = publicaciones.alternarReaccion(publicacionId);
            panelPublicaciones.actualizarReaccion(publicacion);
            limpiarMensaje();
        } catch (IllegalArgumentException | IllegalStateException error) {
            mostrarError(error.getMessage());
        }
    }

    //Pide confirmación antes de borrar una publicación con sus comentarios.
    private boolean confirmarEliminacion() {
        return confirmar("¿Eliminar esta publicación? También se borrarán sus comentarios y respuestas.",
                "Eliminar publicación");
    }

    //Recibe el mensaje y el título. Muestra una alerta de confirmación.
    private boolean confirmar(String texto, String titulo) {
        var alerta = new Alert(Alert.AlertType.CONFIRMATION, texto, ButtonType.CANCEL, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        return alerta.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }

    //Recibe el mensaje de error y lo muestra en el perfil.
    private void mostrarError(String texto) {
        mensaje.getStyleClass().setAll("label", "error");
        mensaje.setText(texto == null || texto.isBlank() ? "No se pudo completar la operación." : texto);
    }

    //Quita el mensaje de error del perfil.
    private void limpiarMensaje() {
        mensaje.getStyleClass().setAll("label");
        mensaje.setText("");
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
