package cr.ac.una.est.socialuna.ui.controller;

import cr.ac.una.est.socialuna.model.entity.AmbitoPublicacion;
import cr.ac.una.est.socialuna.model.entity.TipoPublicacion;
import cr.ac.una.est.socialuna.service.contract.IServicioComunidad;
import cr.ac.una.est.socialuna.service.contract.IServicioUsuarios;
import cr.ac.una.est.socialuna.ui.NavegadorVistas;
import cr.ac.una.est.socialuna.ui.component.PanelPublicaciones;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Objects;

public class ControladorComunidad extends VBox {
    private final IServicioComunidad servicioComunidad;
    private final PanelPublicaciones panel;
    private final MenuButton filtroSede = new MenuButton("Todas las sedes");
    private final HBox filaFiltro = new HBox(10, new Label("Filtrar por sede"), filtroSede);
    private final Label mensaje = new Label();

    private AmbitoPublicacion ambito;
    private String sedeFiltroId;

    //Recibe los servicios, el navegador y el panel para mostrar una comunidad dentro de Inicio.
    public ControladorComunidad(IServicioUsuarios servicioUsuarios, IServicioComunidad servicioComunidad,
                                NavegadorVistas navegador, PanelPublicaciones panel) {
        this.servicioComunidad = Objects.requireNonNull(servicioComunidad);
        this.panel = Objects.requireNonNull(panel);
        setSpacing(12);

        filtroSede.setId("filtroSede");
        agregarOpcionSede(null, "Todas las sedes");
        servicioUsuarios.listarSedes().forEach(sede -> agregarOpcionSede(sede.getId(), sede.getNombre()));
        filaFiltro.setAlignment(Pos.CENTER_LEFT);
        filaFiltro.managedProperty().bind(filaFiltro.visibleProperty());

        mensaje.getStyleClass().add("error");
        mensaje.setWrapText(true);
        mensaje.visibleProperty().bind(mensaje.textProperty().isNotEmpty());
        mensaje.managedProperty().bind(mensaje.visibleProperty());

        panel.alComentar(this::comentar);
        panel.alVerComentarios(this::mostrarComentarios);
        panel.alAbrirPerfil(id -> {
            try {
                navegador.mostrarPerfil(id);
            } catch (IllegalArgumentException | IllegalStateException error) {
                mostrarError(error.getMessage());
            }
        });
        getChildren().addAll(filaFiltro, mensaje, panel.getContenedor());
    }

    //Recibe Sede o Carrera y decide si muestra el filtro de sedes.
    public void cambiarAmbito(AmbitoPublicacion ambito) {
        if (ambito != AmbitoPublicacion.CARRERA && ambito != AmbitoPublicacion.SEDE) {
            throw new IllegalArgumentException("Selecciona la comunidad de carrera o de sede.");
        }
        this.ambito = ambito;
        filaFiltro.setVisible(ambito == AmbitoPublicacion.CARRERA);
    }

    //Carga las publicaciones de la comunidad y aplica el filtro si la pestaña es Carrera.
    public void cargarPublicaciones() {
        mensaje.setText("");
        try {
            var publicaciones = ambito == AmbitoPublicacion.CARRERA
                    ? servicioComunidad.obtenerPorCarrera(sedeFiltroId)
                    : servicioComunidad.obtenerPorSede();
            panel.mostrar(publicaciones, true);
        } catch (IllegalArgumentException | IllegalStateException error) {
            panel.mostrar(new ArrayList<>(), true);
            mostrarError(error.getMessage());
        }
    }

    //Recibe el texto y el tipo, guarda la publicación y quita el filtro si la ocultaría.
    public void publicar(String texto, TipoPublicacion tipo) {
        if (ambito == AmbitoPublicacion.CARRERA) {
            var publicacion = servicioComunidad.publicarEnCarrera(texto, tipo);
            if (sedeFiltroId != null && !sedeFiltroId.equals(publicacion.getSedeId())) {
                sedeFiltroId = null;
                filtroSede.setText("Todas las sedes");
            }
        } else {
            servicioComunidad.publicarEnSede(texto, tipo);
        }
    }

    //Recibe el ID y el nombre de una sede y agrega esa opción al menú del filtro.
    private void agregarOpcionSede(String id, String nombre) {
        var opcion = new MenuItem(nombre);
        opcion.setOnAction(evento -> {
            sedeFiltroId = id;
            filtroSede.setText(nombre);
            cargarPublicaciones();
        });
        filtroSede.getItems().add(opcion);
    }

    //Recibe el ID del post y el texto. Devuelve true si se guardó y actualiza esa tarjeta.
    private boolean comentar(String publicacionId, String texto) {
        try {
            servicioComunidad.comentar(publicacionId, texto);
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
            panel.mostrarComentarios(publicacionId, servicioComunidad.obtenerComentarios(publicacionId));
            mensaje.setText("");
        } catch (IllegalArgumentException | IllegalStateException error) {
            mostrarError(error.getMessage());
        }
    }

    //Recibe el mensaje de error y lo muestra encima de las publicaciones.
    private void mostrarError(String texto) {
        mensaje.setText(texto == null || texto.isBlank() ? "No se pudo completar la operación." : texto);
    }
}
