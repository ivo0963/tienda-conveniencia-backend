package cl.duoc.tienda.validacion;

import java.util.List;
import java.util.Locale;

/** Reglas de validación compartidas por los validadores de Bean Validation y los servicios. */
public final class Validaciones {

    public static final List<String> DOMINIOS_PERMITIDOS = List.of("@duoc.cl", "@profesor.duoc.cl", "@gmail.com");

    private Validaciones() {
    }

    /** RUN sin puntos ni guion con dígito verificador módulo 11 (admite K). Ej: 123456785. */
    public static boolean runValido(String run) {
        if (run == null) {
            return false;
        }
        String limpio = run.toUpperCase(Locale.ROOT).replace(".", "").replace("-", "");
        if (!limpio.matches("[0-9]{7,8}[0-9K]")) {
            return false;
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dv = limpio.charAt(limpio.length() - 1);
        int suma = 0;
        int multiplo = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * multiplo;
            multiplo = multiplo == 7 ? 2 : multiplo + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = resto == 11 ? '0' : resto == 10 ? 'K' : Character.forDigit(resto, 10);
        return dv == esperado;
    }

    public static String normalizarRun(String run) {
        return run.toUpperCase(Locale.ROOT).replace(".", "").replace("-", "").trim();
    }

    public static boolean dominioPermitido(String correo) {
        if (correo == null) {
            return false;
        }
        String c = correo.trim().toLowerCase(Locale.ROOT);
        return DOMINIOS_PERMITIDOS.stream().anyMatch(c::endsWith);
    }
}
