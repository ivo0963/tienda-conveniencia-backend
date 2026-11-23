package cl.duoc.tienda.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Indicadores para el dashboard y los reportes del administrador")
public record ResumenDto(
        @Schema(description = "Suma del total de las boletas no rechazadas", example = "28890") long totalVendido,
        @Schema(example = "3") long cantidadOrdenes,
        @Schema(description = "Boletas no rechazadas", example = "2") long cantidadPagadas,
        @Schema(example = "14445") long ticketPromedio,
        @Schema(example = "10") long cantidadProductos,
        @Schema(description = "Suma del stock de todos los productos", example = "199") long stockTotal,
        @Schema(example = "3") long cantidadCriticos,
        @Schema(description = "Suma de precio × stock", example = "385330") long inventarioValorizado,
        @Schema(example = "3") long cantidadUsuarios,
        @Schema(example = "1") long cantidadClientes,
        List<VentaProducto> ventasPorProducto,
        List<VentaCategoria> ventasPorCategoria,
        List<OrdenesEstado> ordenesPorEstado) {

    public record VentaProducto(Long productoId, String nombre, long unidades, long monto) {
    }

    public record VentaCategoria(Long categoriaId, String nombre, long monto) {
    }

    public record OrdenesEstado(String estado, long cantidad) {
    }
}
