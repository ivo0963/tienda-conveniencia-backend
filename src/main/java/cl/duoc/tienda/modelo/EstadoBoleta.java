package cl.duoc.tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Estados de una boleta. Una boleta rechazada no descuenta stock y no cambia de estado. */
public enum EstadoBoleta {
    PAGADA,
    ENVIADA,
    ENTREGADA,
    RECHAZADA;

    @JsonValue
    public String valor() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static EstadoBoleta desde(String valor) {
        return valor == null ? null : EstadoBoleta.valueOf(valor.trim().toUpperCase());
    }
}
