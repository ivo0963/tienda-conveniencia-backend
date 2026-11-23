package cl.duoc.tienda.dto;

import cl.duoc.tienda.modelo.EstadoUsuario;
import cl.duoc.tienda.modelo.Rol;
import cl.duoc.tienda.modelo.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuario del sistema (nunca incluye la contraseña)")
public record UsuarioDto(
        @Schema(example = "3") Long id,
        @Schema(example = "123456785") String run,
        @Schema(example = "Juan") String nombre,
        @Schema(example = "Pérez") String apellidos,
        @Schema(example = "juan.perez@gmail.com") String correo,
        @Schema(example = "cliente") Rol rol,
        @Schema(example = "activo") EstadoUsuario estado,
        @Schema(example = "Metropolitana") String region,
        @Schema(example = "Providencia") String comuna,
        @Schema(example = "Av. Providencia 1234") String direccion,
        @Schema(example = "Depto 603") String departamento) {

    public static UsuarioDto desde(Usuario u) {
        return new UsuarioDto(u.getId(), u.getRun(), u.getNombre(), u.getApellidos(), u.getCorreo(), u.getRol(),
                u.getEstado(), u.getRegion(), u.getComuna(), u.getDireccion(), u.getDepartamento());
    }
}
