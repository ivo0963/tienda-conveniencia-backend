package cl.duoc.tienda.dto;

import cl.duoc.tienda.modelo.EstadoBoleta;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Nuevo estado de la boleta: pagada, enviada o entregada")
public record CambioEstadoRequest(
        @Schema(example = "enviada") @NotNull(message = "El estado es obligatorio.") EstadoBoleta estado) {
}
