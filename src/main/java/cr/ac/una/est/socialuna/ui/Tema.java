package cr.ac.una.est.socialuna.ui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;

public class Tema {

    //true si el tema activo es el claro (blanco). Se elige arrancando con -Dsocialuna.tema=light.
    private static boolean claro = "light".equalsIgnoreCase(System.getProperty("socialuna.tema", "dark"));

    //Aplica el tema activo a toda la aplicación.
    public static void aplicar() {
        Application.setUserAgentStylesheet(claro
                ? new PrimerLight().getUserAgentStylesheet()
                : new PrimerDark().getUserAgentStylesheet());
    }

    //Alterna entre el tema claro y el oscuro y lo aplica de inmediato.
    public static void alternar() {
        claro = !claro;
        aplicar();
    }

    //Devuelve true cuando el tema que está puesto es el claro.
    static boolean esClaro() {
        return claro;
    }
}
