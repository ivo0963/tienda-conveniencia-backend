package cl.duoc.tienda.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos para crear o editar una categoría")
public record CategoriaRequest(
        @Schema(example = "Congelados") @NotBlank(message = "El nombre es obligatorio.") @Size(max = 60) String nombre,
        @Schema(example = "Helados, verduras y comidas congeladas.") @Size(max = 200) String descripcion,
        @Schema(example = "/img/sin-imagen.svg") @Size(max = 255) String imagen) {
}
