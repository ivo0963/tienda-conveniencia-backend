package cl.duoc.tienda.modelo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Roles del sistema. En JSON se envían en minúsculas: "admin", "vendedor", "cliente". */
public enum Rol {
    ADMIN,
    VENDEDOR,
    CLIENTE;

    @JsonValue
    public String valor() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static Rol desde(String valor) {
        return valor == null ? null : Rol.valueOf(valor.trim().toUpperCase());
    }
}
