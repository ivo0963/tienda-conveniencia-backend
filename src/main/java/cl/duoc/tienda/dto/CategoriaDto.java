package cl.duoc.tienda.dto;

import cl.duoc.tienda.modelo.Categoria;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Categoría de productos")
public record CategoriaDto(
        @Schema(example = "1") Long id,
        @Schema(example = "Bebidas y Licores") String nombre,
        @Schema(example = "Bebidas, jugos, aguas y cervezas.") String descripcion,
        @Schema(example = "/img/coca cola.jpg") String imagen,
        @Schema(description = "Cantidad de productos de la categoría", example = "3") long cantidadProductos) {

    public static CategoriaDto desde(Categoria c, long cantidadProductos) {
        return new CategoriaDto(c.getId(), c.getNombre(), c.getDescripcion(), c.getImagen(), cantidadProductos);
    }
}
