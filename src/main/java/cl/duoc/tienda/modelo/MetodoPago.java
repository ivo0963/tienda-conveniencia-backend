package cl.duoc.tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MetodoPago {
    DEBITO,
    CREDITO,
    TRANSFERENCIA;

    @JsonValue
    public String valor() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static MetodoPago desde(String valor) {
        return valor == null ? null : MetodoPago.valueOf(valor.trim().toUpperCase());
    }
}
