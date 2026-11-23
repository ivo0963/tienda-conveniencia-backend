package cl.duoc.tienda.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.tienda.modelo.DetalleBoleta;

public interface DetalleBoletaRepositorio extends JpaRepository<DetalleBoleta, Long> {

    /** Antes de eliminar un producto se desvincula de las boletas; el detalle conserva nombre y precio. */
    @Modifying
    @Query("update DetalleBoleta d set d.producto = null where d.producto.id = :productoId")
    int desvincularProducto(@Param("productoId") Long productoId);
}
