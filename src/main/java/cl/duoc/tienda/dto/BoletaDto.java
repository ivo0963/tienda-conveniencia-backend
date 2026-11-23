package cl.duoc.tienda.dto;

import java.time.LocalDateTime;
import java.util.List;

import cl.duoc.tienda.modelo.Boleta;
import cl.duoc.tienda.modelo.DetalleBoleta;
import cl.duoc.tienda.modelo.EstadoBoleta;
import cl.duoc.tienda.modelo.MetodoPago;
import cl.duoc.tienda.modelo.TipoEntrega;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Boleta u orden de compra con su detalle")
public record BoletaDto(
        @Schema(example = "4") Long id,
        @Schema(example = "ORD00004") String codigo,
        LocalDateTime fecha,
        @Schema(description = "null si la compra fue como invitado", example = "3") Long usuarioId,
        Cliente cliente,
        Direccion direccion,
        @Schema(example = "domicilio") TipoEntrega entrega,
        @Schema(example = "debito") MetodoPago metodoPago,
        List<Item> items,
        @Schema(example = "9470") Integer subtotal,
        @Schema(example = "2000") Integer envio,
        @Schema(example = "11470") Integer total,
        @Schema(example = "pagada") EstadoBoleta estado,
        @Schema(description = "Causa del rechazo; null si fue pagada") String motivoRechazo) {

    public record Cliente(String nombre, String apellidos, String correo) {
    }

    public record Direccion(String calle, String departamento, String region, String comuna, String indicaciones) {
    }

    public record Item(Long productoId, String nombre, Integer precio, Integer cantidad, Integer subtotal, String imagen) {

        static Item desde(DetalleBoleta d) {
            Long productoId = d.getProducto() != null ? d.getProducto().getId() : null;
            return new Item(productoId, d.getNombreProducto(), d.getPrecioUnitario(), d.getCantidad(),
                    d.getSubtotal(), d.getImagen());
        }
    }

    public static BoletaDto desde(Boleta b) {
        return new BoletaDto(
                b.getId(),
                b.getCodigo(),
                b.getFecha(),
                b.getUsuario() != null ? b.getUsuario().getId() : null,
                new Cliente(b.getClienteNombre(), b.getClienteApellidos(), b.getClienteCorreo()),
                new Direccion(b.getCalle(), b.getDepartamento(), b.getRegion(), b.getComuna(), b.getIndicaciones()),
                b.getEntrega(),
                b.getMetodoPago(),
                b.getDetalles().stream().map(Item::desde).toList(),
                b.getSubtotal(),
                b.getEnvio(),
                b.getTotal(),
                b.getEstado(),
                b.getMotivoRechazo());
    }
}
