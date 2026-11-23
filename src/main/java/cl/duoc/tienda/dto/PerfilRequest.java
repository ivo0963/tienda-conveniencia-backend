package cl.duoc.tienda.dto;

import cl.duoc.tienda.validacion.CorreoPermitido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos que el usuario conectado puede cambiar de su propia cuenta (no el rol ni el estado)")
public record PerfilRequest(
        @Schema(example = "Admin")
        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 50) String nombre,

        @Schema(example = "Duoc")
        @NotBlank(message = "Los apellidos son obligatorios.") @Size(max = 100) String apellidos,

        @Schema(example = "admin@duoc.cl")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Formato de correo inválido.") @CorreoPermitido @Size(max = 100) String correo,

        @Schema(description = "Opcional: nueva contraseña", example = "nueva123")
        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres.") String password,

        @Size(max = 50) String region,
        @Size(max = 50) String comuna,
        @Size(max = 300) String direccion,
        @Size(max = 20) String departamento) {
}
