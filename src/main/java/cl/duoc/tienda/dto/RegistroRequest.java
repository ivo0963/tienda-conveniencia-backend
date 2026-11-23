package cl.duoc.tienda.dto;

import cl.duoc.tienda.validacion.CorreoPermitido;
import cl.duoc.tienda.validacion.RunValido;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Registro público de un cliente")
public record RegistroRequest(
        @Schema(example = "201234565")
        @NotBlank(message = "El RUN es obligatorio.") @RunValido String run,

        @Schema(example = "Camila")
        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 50) String nombre,

        @Schema(example = "Soto")
        @NotBlank(message = "Los apellidos son obligatorios.") @Size(max = 100) String apellidos,

        @Schema(example = "camila.soto@gmail.com")
        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "Formato de correo inválido.") @CorreoPermitido @Size(max = 100) String correo,

        @Schema(example = "clave1")
        @NotBlank(message = "La contraseña es obligatoria.")
        @Size(min = 4, max = 10, message = "La contraseña debe tener entre 4 y 10 caracteres.") String password,

        @Schema(example = "Valparaíso")
        @NotBlank(message = "Debe seleccionar una región.") @Size(max = 50) String region,

        @Schema(example = "Quilpué")
        @NotBlank(message = "Debe seleccionar una comuna.") @Size(max = 50) String comuna,

        @Schema(example = "Los Aromos 123") @Size(max = 300) String direccion,
        @Size(max = 20) String departamento) {
}
