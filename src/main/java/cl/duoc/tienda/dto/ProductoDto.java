package cl.duoc.tienda.dto;

import cl.duoc.tienda.modelo.Producto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Producto del catálogo")
public record ProductoDto(
        @Schema(example = "2") Long id,
        @Schema(example = "SNK-001") String codigo,
        @Schema(example = "Papas Fritas 250 g") String nombre,
        @Schema(example = "Papas fritas corte americano.") String descripcion,
        @Schema(description = "Precio normal en CLP", example = "1890") Integer precio,
        @Schema(description = "Precio de oferta en CLP; null si no está en oferta", example = "1490") Integer precioOferta,
        @Schema(example = "20") Integer stock,
        @Schema(example = "5") Integer stockCritico,
        @Schema(example = "/img/papas fritas.jpg") String imagen,
        @Schema(example = "2") Long categoriaId,
        @Schema(example = "Snacks y Golosinas") String categoriaNombre) {

    public static ProductoDto desde(Producto p) {
        return new ProductoDto(p.getId(), p.getCodigo(), p.getNombre(), p.getDescripcion(), p.getPrecio(),
                p.getPrecioOferta(), p.getStock(), p.getStockCritico(), p.getImagen(),
                p.getCategoria().getId(), p.getCategoria().getNombre());
    }
}
