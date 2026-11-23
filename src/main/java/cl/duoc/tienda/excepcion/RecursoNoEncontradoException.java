package cl.duoc.tienda.excepcion;

/** Se traduce a una respuesta 404. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String recurso, Long id) {
        super(recurso + " con id " + id + " no existe.");
    }
}
