package cl.duoc.tienda.dto;

import java.util.List;

import cl.duoc.tienda.modelo.MetodoPago;
import cl.duoc.tienda.modelo.TipoEntrega;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos del checkout. Los precios no se envían: el backend los toma de la base de datos.")
public record CompraRequest(
        @Valid @NotNull(message = "Faltan los datos del cliente.") Cliente cliente,

        @Valid Direccion direccion,

        @Schema(example = "domicilio")
        @NotNull(message = "Seleccione una opción de entrega.") TipoEntrega entrega,

        @Schema(example = "debito")
        @NotNull(message = "Seleccione un medio de pago.") MetodoPago metodoPago,

        @Valid @NotEmpty(message = "El carrito está vacío.") List<Item> items,

        @Schema(description = "Opcional, solo para la demostración: fuerza el rechazo del pago", example = "false")
        Boolean simularRechazo) {

    public boolean debeSimularRechazo() {
        return Boolean.TRUE.equals(simularRechazo);
    }

    public record Cliente(
            @Schema(example = "Juan")
            @NotBlank(message = "El nombre es obligatorio.") @Size(max = 50) String nombre,

            @Schema(example = "Pérez")
            @NotBlank(message = "Los apellidos son obligatorios.") @Size(max = 100) String apellidos,

            @Schema(example = "juan.perez@gmail.com")
            @NotBlank(message = "El correo es obligatorio.")
            @Email(message = "Formato de correo inválido.") @Size(max = 100) String correo) {
    }

    public record Direccion(
            @Schema(example = "Av. Providencia 1234") @Size(max = 150) String calle,
            @Schema(example = "Depto 603") @Size(max = 20) String departamento,
            @Schema(example = "Metropolitana") @Size(max = 50) String region,
            @Schema(example = "Providencia") @Size(max = 50) String comuna,
            @Schema(example = "Dejar con el conserje.") @Size(max = 300) String indicaciones) {
    }

    public record Item(
            @Schema(example = "1") @NotNull(message = "Falta el producto.") Long productoId,

            @Schema(example = "2")
            @NotNull(message = "Falta la cantidad.")
            @Positive(message = "La cantidad debe ser mayor a 0.") Integer cantidad) {
    }
}
