package cl.duoc.tienda.dto;

import cl.duoc.tienda.modelo.EstadoUsuario;
import cl.duoc.tienda.modelo.Rol;
import cl.duoc.tienda.validacion.CorreoPermitido;
import cl.duoc.tienda.validacion.RunValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para que un administrador cree o edite un usuario")
public record UsuarioRequest(
        @Schema(example = "201234565")
        @NotBlank(message = "El RUN es obligatorio.") @RunValido String run,

        @Schema(example = "Camila")
        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 50) String nombre,

        @Schema(example = "Soto")
        @NotBlank(message = "Los apellidos son obligatorios.") @Size(max = 100) String apellidos,

        @Schema(example = "camila.soto@duoc.cl")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Formato de correo inválido.") @CorreoPermitido @Size(max = 100) String correo,

        @Schema(description = "Obligatoria al crear; si se omite al editar se mantiene la actual", example = "clave123")
        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres.") String password,

        @Schema(example = "vendedor")
        @NotNull(message = "Seleccione un rol.") Rol rol,

        @Schema(example = "activo") EstadoUsuario estado,

        @Schema(example = "Valparaíso") @Size(max = 50) String region,
        @Schema(example = "Quilpué") @Size(max = 50) String comuna,
        @Schema(example = "Los Aromos 123") @Size(max = 300) String direccion,
        @Size(max = 20) String departamento) {
}
