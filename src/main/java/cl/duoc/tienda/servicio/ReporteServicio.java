package cl.duoc.tienda.servicio;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tienda.dto.ResumenDto;
import cl.duoc.tienda.modelo.Boleta;
import cl.duoc.tienda.modelo.DetalleBoleta;
import cl.duoc.tienda.modelo.EstadoBoleta;
import cl.duoc.tienda.modelo.Producto;
import cl.duoc.tienda.modelo.Rol;
import cl.duoc.tienda.repositorio.BoletaRepositorio;
import cl.duoc.tienda.repositorio.CategoriaRepositorio;
import cl.duoc.tienda.repositorio.ProductoRepositorio;
import cl.duoc.tienda.repositorio.UsuarioRepositorio;

@Service
public class ReporteServicio {

    private final BoletaRepositorio boletas;
    private final ProductoRepositorio productos;
    private final CategoriaRepositorio categorias;
    private final UsuarioRepositorio usuarios;

    public ReporteServicio(BoletaRepositorio boletas, ProductoRepositorio productos, CategoriaRepositorio categorias,
                           UsuarioRepositorio usuarios) {
        this.boletas = boletas;
        this.productos = productos;
        this.categorias = categorias;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public ResumenDto resumen() {
        List<Boleta> todas = boletas.findAllByOrderByFechaDesc();
        List<Boleta> pagadas = todas.stream().filter(b -> b.getEstado() != EstadoBoleta.RECHAZADA).toList();
        List<Producto> catalogo = productos.findAllByOrderByIdAsc();
        long totalVendido = pagadas.stream().mapToLong(Boleta::getTotal).sum();

        // Ventas por producto; si el producto fue eliminado se agrupa por el nombre guardado en la boleta.
        Map<String, Acumulado> porProducto = new LinkedHashMap<>();
        for (Boleta b : pagadas) {
            for (DetalleBoleta d : b.getDetalles()) {
                Long id = d.getProducto() != null ? d.getProducto().getId() : null;
                String clave = id != null ? "id:" + id : "nombre:" + d.getNombreProducto();
                Acumulado a = porProducto.computeIfAbsent(clave, k -> new Acumulado(id, d.getNombreProducto()));
                a.unidades += d.getCantidad();
                a.monto += d.getSubtotal();
            }
        }
        List<ResumenDto.VentaProducto> ventasPorProducto = porProducto.values().stream()
                .map(a -> new ResumenDto.VentaProducto(a.productoId, a.nombre, a.unidades, a.monto))
                .sorted(Comparator.comparingLong(ResumenDto.VentaProducto::monto).reversed())
                .toList();

        Map<Long, Long> categoriaDeProducto = new LinkedHashMap<>();
        catalogo.forEach(p -> categoriaDeProducto.put(p.getId(), p.getCategoria().getId()));
        List<ResumenDto.VentaCategoria> ventasPorCategoria = categorias.findAll().stream()
                .map(c -> new ResumenDto.VentaCategoria(c.getId(), c.getNombre(), ventasPorProducto.stream()
                        .filter(v -> v.productoId() != null && c.getId().equals(categoriaDeProducto.get(v.productoId())))
                        .mapToLong(ResumenDto.VentaProducto::monto)
                        .sum()))
                .toList();

        List<ResumenDto.OrdenesEstado> ordenesPorEstado = Arrays.stream(EstadoBoleta.values())
                .map(estado -> new ResumenDto.OrdenesEstado(estado.valor(), todas.stream().filter(b -> b.getEstado() == estado).count()))
                .toList();

        return new ResumenDto(
                totalVendido,
                todas.size(),
                pagadas.size(),
                pagadas.isEmpty() ? 0 : Math.round((double) totalVendido / pagadas.size()),
                catalogo.size(),
                catalogo.stream().mapToLong(Producto::getStock).sum(),
                catalogo.stream().filter(Producto::esStockCritico).count(),
                catalogo.stream().mapToLong(p -> (long) p.getPrecio() * p.getStock()).sum(),
                usuarios.count(),
                usuarios.countByRol(Rol.CLIENTE),
                ventasPorProducto,
                ventasPorCategoria,
                ordenesPorEstado);
    }

    private static final class Acumulado {
        private final Long productoId;
        private final String nombre;
        private long unidades;
        private long monto;

        private Acumulado(Long productoId, String nombre) {
            this.productoId = productoId;
            this.nombre = nombre;
        }
    }
}
