package cr.ac.una.est.socialuna.ui.component;

import cr.ac.una.est.socialuna.model.entity.Comentario;
import cr.ac.una.est.socialuna.model.entity.Publicacion;
import cr.ac.una.est.socialuna.model.entity.Usuario;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PanelPublicaciones {
    private final VBox contenedor = new VBox(12);
    private final IServicioUsuarios servicioUsuarios;
    private final Map<String, VBox> comentariosPorPublicacion = new HashMap<>();
    private final ObjectProperty<BiConsumer<String, String>> accionComentar = new SimpleObjectProperty<>();
    private final ObjectProperty<Consumer<String>> accionVerComentarios = new SimpleObjectProperty<>();
    private final ObjectProperty<Consumer<String>> accionVerPerfil = new SimpleObjectProperty<>();

    //Recibe el servicio de usuarios para buscar el nombre de cada autor.
    public PanelPublicaciones(IServicioUsuarios servicioUsuarios) {
        this.servicioUsuarios = Objects.requireNonNull(servicioUsuarios);
    }

    //Recibe la lista de posts y si se puede comentar. Redibuja las tarjetas con esa configuración.
    public void mostrar(ArrayList<Publicacion> publicaciones, boolean permitirComentarios) {
        contenedor.getChildren().clear();
        comentariosPorPublicacion.clear();
        if (publicaciones == null || publicaciones.isEmpty()) {
            contenedor.getChildren().add(new Label("Todavía no hay publicaciones."));
            return;
        }
        for (Publicacion publicacion : publicaciones) {
            contenedor.getChildren().add(crearTarjeta(publicacion, permitirComentarios));
        }
    }

    //Recibe el ID del post y la lista de comentarios. Actualiza los comentarios de su tarjeta.
    public void mostrarComentarios(String publicacionId, ArrayList<Comentario> comentarios) {
        VBox lista = comentariosPorPublicacion.get(publicacionId);
        if (lista == null) return;
        lista.getChildren().clear();
        if (comentarios == null || comentarios.isEmpty()) {
            lista.getChildren().add(new Label("Todavía no hay comentarios."));
        } else {
            for (Comentario comentario : comentarios) {
                lista.getChildren().add(crearFilaComentario(comentario));
            }
        }
    }

    //Registra qué hacer al enviar un comentario. La acción recibe el ID del post y el texto.
    public void alComentar(BiConsumer<String, String> accion) {
        accionComentar.set(accion);
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
        var tarjeta = new VBox(6, crearAutor(publicacion.getAutorId()), texto);
        if (permitirComentarios) {
            agregarControlesDeComentarios(tarjeta, publicacion.getId());
        }
        return tarjeta;
    }

    //Recibe la tarjeta y el ID del post. Agrega la lista, el campo de texto y los botones de comentarios.
    private void agregarControlesDeComentarios(VBox tarjeta, String publicacionId) {
        var lista = new VBox(6);
        lista.visibleProperty().bind(Bindings.isNotEmpty(lista.getChildren()));
        lista.managedProperty().bind(lista.visibleProperty());
        comentariosPorPublicacion.put(publicacionId, lista);

        var ver = new Button("Ver comentarios");
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
            if (accion != null && comentario != null && !comentario.isBlank()) {
                accion.accept(publicacionId, comentario.strip());
                entrada.clear();
            }
        });
        entrada.setOnAction(evento -> enviar.fire());
        tarjeta.getChildren().addAll(ver, lista, new HBox(6, entrada, enviar));
    }

    //Recibe un comentario y devuelve una fila con su autor y texto.
    private HBox crearFilaComentario(Comentario comentario) {
        Label texto = new Label(comentario.getTexto());
        texto.setWrapText(true);
        texto.setMinWidth(0);
        HBox.setHgrow(texto, Priority.ALWAYS);
        return new HBox(6, crearAutor(comentario.getAutorId()), texto);
    }

    //Busca al autor por ID y devuelve un enlace a su perfil, o un aviso si no aparece.
    private Node crearAutor(String autorId) {
        Usuario autor = servicioUsuarios.obtenerPerfil(autorId);
        if (autor == null) return new Label("Autor no disponible");
        var enlace = new Hyperlink(autor.getNombre());
        enlace.disableProperty().bind(accionVerPerfil.isNull());
        enlace.setOnAction(evento -> {
            var accion = accionVerPerfil.get();
            if (accion != null) accion.accept(autorId);
        });
        return enlace;
    }
}
