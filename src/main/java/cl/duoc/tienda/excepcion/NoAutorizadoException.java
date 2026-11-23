package cl.duoc.tienda.excepcion;

/** Falta la sesión o el token de refresco no es válido. Se traduce a 401. */
public class NoAutorizadoException extends RuntimeException {

    public NoAutorizadoException(String mensaje) {
        super(mensaje);
    }
}
