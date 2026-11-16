package cr.ac.una.est.socialuna.ui.component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class Fechas {

    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Fechas() { }

    //Recibe una fecha en milisegundos y la devuelve como texto relativo a ahora.
    public static String relativa(long creadoEn) {
        return relativa(creadoEn, System.currentTimeMillis());
    }

    //Recibe la fecha y el momento de referencia. Devuelve "hace X" o la fecha corta.
    public static String relativa(long creadoEn, long ahora) {
        long segundos = Math.max(0, (ahora - creadoEn) / 1000);
        if (segundos < 60) return "hace un momento";

        long minutos = segundos / 60;
        if (minutos < 60) return "hace " + minutos + " min";

        long horas = minutos / 60;
        if (horas < 24) return "hace " + horas + " h";

        long dias = horas / 24;
        if (dias < 7) return "hace " + dias + " d";

        return FECHA_CORTA.format(Instant.ofEpochMilli(creadoEn).atZone(ZoneId.systemDefault()));
    }
}
