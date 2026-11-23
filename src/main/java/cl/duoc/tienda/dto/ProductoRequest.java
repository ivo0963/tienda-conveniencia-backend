package cl.duoc.tienda.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o editar un producto")
public record ProductoRequest(
        @Schema(example = "CON-001")
        @NotBlank(message = "El código es obligatorio.") @Size(max = 20) String codigo,

        @Schema(example = "Helado 1 L")
        @NotBlank(message = "El nombre es obligatorio.") @Size(max = 100) String nombre,

        @Schema(example = "Helado de vainilla.")
        @Size(max = 500) String descripcion,

        @Schema(example = "3500")
        @NotNull(message = "El precio es obligatorio.")
        @PositiveOrZero(message = "El precio debe ser mayor o igual a 0.") Integer precio,

        @Schema(description = "Opcional; debe ser menor al precio normal", example = "2990")
        @PositiveOrZero(message = "La oferta debe ser mayor o igual a 0.") Integer precioOferta,

        @Schema(example = "8")
        @NotNull(message = "El stock es obligatorio.")
        @PositiveOrZero(message = "El stock debe ser mayor o igual a 0.") Integer stock,

        @Schema(example = "2")
        @NotNull(message = "El stock crítico es obligatorio.")
        @PositiveOrZero(message = "El stock crítico debe ser mayor o igual a 0.") Integer stockCritico,

        @Schema(example = "/img/sin-imagen.svg")
        @Size(max = 255) String imagen,

        @Schema(example = "4")
        @NotNull(message = "Seleccione una categoría.") Long categoriaId) {
}
