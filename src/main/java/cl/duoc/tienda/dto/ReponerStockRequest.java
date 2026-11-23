package cl.duoc.tienda.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Unidades que se suman al stock actual")
public record ReponerStockRequest(
        @Schema(example = "20")
        @NotNull(message = "La cantidad es obligatoria.")
        @Positive(message = "La cantidad debe ser mayor a 0.") Integer cantidad) {
}
