package cr.ac.una.est.socialuna.ui.component;

import cr.ac.una.est.socialuna.model.entity.Comentario;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PanelPublicaciones {
    private final VBox contenedor = new VBox(12);
    private final IServicioUsuarios servicioUsuarios;
    private final Map<String, VBox> comentariosPorPublicacion = new HashMap<>();
    private final Map<String, Button> botonesComentarios = new HashMap<>();
    private final Map<String, Button> botonesReaccion = new HashMap<>();
    private final ObjectProperty<BiPredicate<String, String>> accionComentar = new SimpleObjectProperty<>();
    private final ObjectProperty<AccionResponder> accionResponder = new SimpleObjectProperty<>();
    private final ObjectProperty<AccionEliminarComentario> accionEliminarComentario = new SimpleObjectProperty<>();
    private final ObjectProperty<Consumer<String>> accionVerComentarios = new SimpleObjectProperty<>();
    private final ObjectProperty<Consumer<String>> accionVerPerfil = new SimpleObjectProperty<>();
    private final ObjectProperty<Consumer<String>> accionReaccionar = new SimpleObjectProperty<>();
    private final ObjectProperty<Predicate<String>> accionEliminar = new SimpleObjectProperty<>();

    //Acción que recibe el post, el comentario padre y el texto, y devuelve true si guardó la respuesta.
    @FunctionalInterface
    public interface AccionResponder {
        boolean test(String publicacionId, String comentarioPadreId, String texto);
    }

    //Acción que recibe el post y el comentario, y devuelve true si borró el comentario.
    @FunctionalInterface
    public interface AccionEliminarComentario {
        boolean test(String publicacionId, String comentarioId);
    }

    //Recibe el servicio de usuarios para buscar el nombre de cada autor.
    public PanelPublicaciones(IServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = Objects.requireNonNull(servicioUsuarios);
        contenedor.setId("listaTarjetas");
    }

    //Recibe la lista de posts y si se puede comentar. Redibuja las tarjetas con esa configuración.
    public void mostrar(ArrayList<Publicacion> publicaciones, boolean permitirComentarios) {
        contenedor.getChildren().clear();
        comentariosPorPublicacion.clear();
        botonesComentarios.clear();
        botonesReaccion.clear();
        if (publicaciones == null || publicaciones.isEmpty()) {
            contenedor.getChildren().add(new Label("Todavía no hay publicaciones."));
            return;
        }
        for (Publicacion publicacion : publicaciones) {
            contenedor.getChildren().add(crearTarjeta(publicacion, permitirComentarios));
        }
    }

    //Recibe el ID del post y la lista de comentarios. Pinta cada hilo con sus respuestas indentadas.
    public void mostrarComentarios(String publicacionId, ArrayList<Comentario> comentarios) {
        VBox lista = comentariosPorPublicacion.get(publicacionId);
        if (lista == null) return;

        Button ver = botonesComentarios.get(publicacionId);
        if (ver != null) ver.setText(textoComentarios(comentarios == null ? 0 : comentarios.size()));

        lista.getChildren().clear();
        if (comentarios == null || comentarios.isEmpty()) {
            lista.getChildren().add(new Label("Todavía no hay comentarios."));
            return;
        }

        var porId = new HashMap<String, Comentario>();
        for (Comentario comentario : comentarios) porId.put(comentario.getId(), comentario);

        for (Comentario comentario : comentarios) {
            if (raizDe(comentario, porId) == comentario) {
                lista.getChildren().add(crearHilo(publicacionId, comentario, comentarios, porId));
            }
        }
    }

    //Recibe una raíz y pinta su comentario junto a las primeras respuestas de su hilo.
    private VBox crearHilo(String publicacionId, Comentario raiz, ArrayList<Comentario> comentarios,
                           Map<String, Comentario> porId) {
        var hilo = new VBox(6);
        hilo.getChildren().add(crearFilaComentario(publicacionId, raiz, null));

        var respuestas = new ArrayList<Comentario>();
        for (Comentario respuesta : comentarios) {
            if (respuesta != raiz && raizDe(respuesta, porId) == raiz) {
                respuestas.add(respuesta);
            }
        }

        for (int i = 0; i < Math.min(3, respuestas.size()); i++) {
            hilo.getChildren().add(crearFilaComentario(publicacionId, respuestas.get(i),
                    porId.get(respuestas.get(i).getRespuestaAId())));
        }

        if (respuestas.size() > 3) {
            var restantes = new ArrayList<>(respuestas.subList(3, respuestas.size()));
            var enlace = new Hyperlink("Ver " + restantes.size() + " respuestas");
            enlace.setOnAction(evento -> {
                hilo.getChildren().remove(enlace);
                for (Comentario respuesta : restantes) {
                    hilo.getChildren().add(crearFilaComentario(publicacionId, respuesta,
                            porId.get(respuesta.getRespuestaAId())));
                }
            });
            hilo.getChildren().add(enlace);
        }

        return hilo;
    }

    //Recibe un comentario y devuelve la raíz del hilo al que pertenece.
    private Comentario raizDe(Comentario comentario, Map<String, Comentario> porId) {
        Comentario actual = comentario;
        while (actual.getRespuestaAId() != null) {
            Comentario padre = porId.get(actual.getRespuestaAId());
            if (padre == null || padre == actual) break;
            actual = padre;
        }
        return actual;
    }

    //Registra una acción que recibe el ID y el texto y devuelve true si guardó el comentario.
    public void alComentar(BiPredicate<String, String> accion) {
        accionComentar.set(accion);
    }

    //Registra una acción que recibe el post, el comentario padre y el texto y devuelve true si guardó.
    public void alResponder(AccionResponder accion) {
        accionResponder.set(accion);
    }

    //Registra qué hacer al pulsar Eliminar. La acción recibe el ID del post y devuelve true si borró.
    public void alEliminarPublicacion(Predicate<String> accion) {
        accionEliminar.set(accion);
    }

    //Registra qué hacer al pulsar Eliminar en un comentario. Devuelve true si borró.
    public void alEliminarComentario(AccionEliminarComentario accion) {
        accionEliminarComentario.set(accion);
    }

    //Registra qué hacer al pulsar Me gusta. La acción recibe el ID del post.
    public void alReaccionar(Consumer<String> accion) {
        accionReaccionar.set(accion);
    }

    //Recibe la publicación actualizada y refresca el botón de me gusta de su tarjeta.
    public void actualizarReaccion(Publicacion publicacion) {
        Button boton = botonesReaccion.get(publicacion.getId());
        if (boton != null) actualizarBotonReaccion(boton, publicacion);
    }

    //Registra qué hacer al pulsar Ver comentarios. La acción recibe el ID del post.
    public void alVerComentarios(Consumer<String> accion) {
        accionVerComentarios.set(accion);
    }

    //Registra qué hacer al pulsar un autor. La acción recibe el ID de su perfil.
    public void alAbrirPerfil(Consumer<String> accion) {
        accionVerPerfil.set(accion);
    }

    //Devuelve el VBox donde se muestran las tarjetas.
    public VBox getContenedor() {
        return contenedor;
    }

    //Recibe el post y si se puede comentar. Devuelve la tarjeta armada con los controles que toquen.
    private VBox crearTarjeta(Publicacion publicacion, boolean permitirComentarios) {
        Label texto = new Label(publicacion.getTexto());
        texto.setWrapText(true);

        boolean pregunta = publicacion.getTipo() == TipoPublicacion.PREGUNTA;
        var tipo = new Label(pregunta ? "Pregunta" : "Publicación");
        tipo.getStyleClass().add(pregunta ? "tipo-pregunta" : "tipo-publicacion");
        var espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        var encabezado = new HBox(10, crearAutor(publicacion.getAutorId()), espacio, tipo);
        if (esAutor(publicacion)) encabezado.getChildren().add(crearBotonEliminar(publicacion.getId()));
        encabezado.setAlignment(Pos.CENTER_LEFT);

        var fecha = new Label(Fechas.relativa(publicacion.getCreadoEn()));
        fecha.getStyleClass().add("fecha-publicacion");

        var tarjeta = new VBox(10, encabezado, fecha, texto, crearBotonReaccion(publicacion));
        tarjeta.getStyleClass().add("tarjeta-publicacion");
        if (pregunta) tarjeta.getStyleClass().add("tarjeta-pregunta");
        if (permitirComentarios) {
            agregarControlesDeComentarios(tarjeta, publicacion.getId(), publicacion.getComentarios().size());
        }
        return tarjeta;
    }

    //Recibe la tarjeta, el ID del post y el total de comentarios. Agrega la lista y los botones.
    private void agregarControlesDeComentarios(VBox tarjeta, String publicacionId, int totalComentarios) {
        var lista = new VBox(6);
        lista.visibleProperty().bind(Bindings.isNotEmpty(lista.getChildren()));
        lista.managedProperty().bind(lista.visibleProperty());
        comentariosPorPublicacion.put(publicacionId, lista);

        var ver = new Button(textoComentarios(totalComentarios));
        botonesComentarios.put(publicacionId, ver);
        ver.disableProperty().bind(accionVerComentarios.isNull());
        ver.setOnAction(evento -> {
            var accion = accionVerComentarios.get();
            if (accion != null) accion.accept(publicacionId);
        });

        var entrada = new TextField();
        entrada.setPromptText("Escribe un comentario");
        HBox.setHgrow(entrada, Priority.ALWAYS);
        var enviar = new Button("Comentar");
        enviar.disableProperty().bind(accionComentar.isNull().or(Bindings.createBooleanBinding(
                () -> entrada.getText() == null || entrada.getText().isBlank(), entrada.textProperty())));
        enviar.setOnAction(evento -> {
            var accion = accionComentar.get();
            String comentario = entrada.getText();
            if (accion != null && comentario != null && !comentario.isBlank()
                    && accion.test(publicacionId, comentario.strip())) {
                entrada.clear();
            }
        });
        entrada.setOnAction(evento -> enviar.fire());
        tarjeta.getChildren().addAll(ver, lista, new HBox(6, entrada, enviar));
    }

    //Recibe un comentario, su post y su padre. Devuelve la fila con autor, texto y respuesta inline.
    private VBox crearFilaComentario(String publicacionId, Comentario comentario, Comentario padre) {
        var texto = new Label(comentario.getTexto());
        texto.setWrapText(true);
        texto.setMinWidth(0);
        HBox.setHgrow(texto, Priority.ALWAYS);

        var fecha = new Label(Fechas.relativa(comentario.getCreadoEn()));
        fecha.getStyleClass().add("fecha-publicacion");

        var responder = new Hyperlink("Responder");
        var fila = new HBox(6, crearAutor(comentario.getAutorId()));
        if (padre != null) {
            var mencion = new Label("↪ " + nombreDe(padre));
            mencion.getStyleClass().add("mencion-respuesta");
            fila.getChildren().add(mencion);
        }
        fila.getChildren().addAll(texto, fecha, responder);
        if (esAutorComentario(comentario)) {
            var eliminar = new Hyperlink("Eliminar");
            eliminar.getStyleClass().add("boton-eliminar");
            eliminar.disableProperty().bind(accionEliminarComentario.isNull());
            eliminar.setOnAction(evento -> {
                var accion = accionEliminarComentario.get();
                if (accion != null) accion.test(publicacionId, comentario.getId());
            });
            fila.getChildren().add(eliminar);
        }
        fila.setAlignment(Pos.TOP_LEFT);

        var entrada = new TextField();
        entrada.setPromptText("Responde a " + nombreDe(comentario));
        HBox.setHgrow(entrada, Priority.ALWAYS);
        var enviar = new Button("Enviar");
        enviar.disableProperty().bind(Bindings.createBooleanBinding(
                () -> accionResponder.get() == null
                        || entrada.getText() == null
                        || entrada.getText().isBlank(),
                entrada.textProperty(), accionResponder));
        var editor = new HBox(6, entrada, enviar);
        editor.setVisible(false);
        editor.managedProperty().bind(editor.visibleProperty());

        responder.setOnAction(evento -> {
            editor.setVisible(!editor.isVisible());
            if (editor.isVisible()) entrada.requestFocus();
        });

        Runnable enviarRespuesta = () -> {
            var accion = accionResponder.get();
            String valor = entrada.getText();
            if (accion != null && valor != null && !valor.isBlank()
                    && accion.test(publicacionId, comentario.getId(), valor.strip())) {
                entrada.clear();
                editor.setVisible(false);
            }
        };
        enviar.setOnAction(evento -> enviarRespuesta.run());
        entrada.setOnAction(evento -> enviarRespuesta.run());

        var celda = new VBox(4, fila, editor);
        if (padre != null) celda.getStyleClass().add("fila-respuesta");
        return celda;
    }

    //Recibe un comentario y devuelve "@usuario" de su autor.
    private String nombreDe(Comentario comentario) {
        Usuario autor = servicioUsuarios.obtenerPerfil(comentario.getAutorId());
        return autor == null ? "@usuario" : "@" + autor.getNombreUsuario();
    }

    //Devuelve true si el usuario en sesión escribió la publicación.
    private boolean esAutor(Publicacion publicacion) {
        Usuario actual = servicioUsuarios.getUsuarioActual();
        return actual != null && actual.getId().equals(publicacion.getAutorId());
    }

    //Devuelve true si el usuario en sesión escribió el comentario.
    private boolean esAutorComentario(Comentario comentario) {
        Usuario actual = servicioUsuarios.getUsuarioActual();
        return actual != null && actual.getId().equals(comentario.getAutorId());
    }

    //Devuelve el botón de eliminar de una publicación propia.
    private Button crearBotonEliminar(String publicacionId) {
        var eliminar = new Button("Eliminar");
        eliminar.getStyleClass().add("boton-eliminar");
        eliminar.disableProperty().bind(accionEliminar.isNull());
        eliminar.setOnAction(evento -> {
            var accion = accionEliminar.get();
            if (accion != null) accion.test(publicacionId);
        });
        return eliminar;
    }

    //Recibe la publicación y devuelve su botón de me gusta ya conectado.
    private Button crearBotonReaccion(Publicacion publicacion) {
        var boton = new Button();
        boton.getStyleClass().add("boton-reaccion");
        boton.disableProperty().bind(accionReaccionar.isNull());
        boton.setOnAction(evento -> {
            var accion = accionReaccionar.get();
            if (accion != null) accion.accept(publicacion.getId());
        });
        botonesReaccion.put(publicacion.getId(), boton);
        actualizarBotonReaccion(boton, publicacion);
        return boton;
    }

    //Recibe el botón y la publicación. Pinta el total y si al usuario actual le gusta.
    private void actualizarBotonReaccion(Button boton, Publicacion publicacion) {
        Usuario actual = servicioUsuarios.getUsuarioActual();
        boolean activa = actual != null && publicacion.leGustaA(actual.getId());
        boton.setText((activa ? "Te gusta" : "Me gusta") + " (" + publicacion.getTotalReacciones() + ")");
        boton.getStyleClass().remove("reaccion-activa");
        if (activa) boton.getStyleClass().add("reaccion-activa");
    }

    //Recibe el total de comentarios y devuelve el texto del botón.
    private String textoComentarios(int total) {
        return total == 0 ? "Ver comentarios" : "Ver comentarios (" + total + ")";
    }

    //Busca al autor por ID y devuelve su avatar con enlace al perfil, o un aviso si no aparece.
    private Node crearAutor(String autorId) {
        Usuario autor = servicioUsuarios.obtenerPerfil(autorId);
        if (autor == null) {
            var caja = new HBox(8, Avatar.crear(null, 32), new Label("Autor no disponible"));
            caja.setAlignment(Pos.CENTER_LEFT);
            return caja;
        }
        var enlace = new Hyperlink(autor.getNombre());
        enlace.disableProperty().bind(accionVerPerfil.isNull());
        enlace.setOnAction(evento -> {
            var accion = accionVerPerfil.get();
            if (accion != null) accion.accept(autorId);
        });
        var caja = new HBox(8, Avatar.crear(autor, 32), enlace);
        caja.setAlignment(Pos.CENTER_LEFT);
        return caja;
    }
}
