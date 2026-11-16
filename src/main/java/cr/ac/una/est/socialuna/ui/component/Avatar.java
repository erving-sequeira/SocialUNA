package cr.ac.una.est.socialuna.ui.component;

import cr.ac.una.est.socialuna.model.entity.Usuario;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public final class Avatar {

    private static final String[] COLORES = {
            "avatar-1", "avatar-2", "avatar-3", "avatar-4", "avatar-5", "avatar-6"
    };

    private Avatar() { }

    //Recibe un perfil y el tamaño. Devuelve un círculo con la inicial y un color fijo por usuario.
    public static StackPane crear(Usuario usuario, double tamano) {
        var circulo = new StackPane();
        circulo.getStyleClass().addAll("avatar", colorDe(usuario));
        circulo.setMinSize(tamano, tamano);
        circulo.setPrefSize(tamano, tamano);
        circulo.setMaxSize(tamano, tamano);

        var letra = new Label(inicial(usuario == null ? null : usuario.getNombre()));
        letra.getStyleClass().add("avatar-letra");
        letra.setStyle("-fx-font-size: " + Math.round(tamano * 0.42) + "px;");
        circulo.getChildren().add(letra);
        return circulo;
    }

    //Recibe el nombre y devuelve su primera letra en mayúscula.
    static String inicial(String nombre) {
        if (nombre == null) return "?";
        String limpio = nombre.strip();
        return limpio.isEmpty() ? "?" : limpio.substring(0, 1).toUpperCase();
    }

    //Recibe el perfil y elige una clase de color estable a partir de su ID.
    private static String colorDe(Usuario usuario) {
        String id = usuario == null ? "" : usuario.getId();
        return COLORES[Math.floorMod(id.hashCode(), COLORES.length)];
    }
}
