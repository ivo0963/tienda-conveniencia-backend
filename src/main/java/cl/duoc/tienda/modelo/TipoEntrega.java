package cl.duoc.tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoEntrega {
    DOMICILIO,
    RETIRO;

    @JsonValue
    public String valor() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static TipoEntrega desde(String valor) {
        return valor == null ? null : TipoEntrega.valueOf(valor.trim().toUpperCase());
    }
}
