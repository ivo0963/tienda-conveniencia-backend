package cl.duoc.tienda.excepcion;

import java.util.Map;

/** Error de validación que depende de otros campos (no expresable con anotaciones). Se traduce a 400. */
public class DatosInvalidosException extends RuntimeException {

    private final Map<String, String> errores;

    public DatosInvalidosException(String campo, String mensaje) {
        super(mensaje);
        this.errores = Map.of(campo, mensaje);
    }

    public Map<String, String> getErrores() {
        return errores;
    }
}
