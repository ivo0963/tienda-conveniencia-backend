package cl.duoc.tienda.excepcion;

/** Una operación válida en formato pero que rompe una regla del negocio. Se traduce a 409. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
