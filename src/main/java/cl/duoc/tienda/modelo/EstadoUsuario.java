package cl.duoc.tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Un usuario inactivo no puede iniciar sesión ni renovar su token. */
public enum EstadoUsuario {
    ACTIVO,
    INACTIVO;

    @JsonValue
    public String valor() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static EstadoUsuario desde(String valor) {
        return valor == null ? null : EstadoUsuario.valueOf(valor.trim().toUpperCase());
    }
}
