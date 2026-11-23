package cl.duoc.tienda.repositorio;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.tienda.modelo.Producto;
import jakarta.persistence.LockModeType;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {

    /** Carga la categoría en la misma consulta para evitar el problema N+1. */
    @EntityGraph(attributePaths = "categoria")
    List<Producto> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "categoria")
    List<Producto> findByCategoriaIdOrderByIdAsc(Long categoriaId);

    @EntityGraph(attributePaths = "categoria")
    @Query("select p from Producto p where p.stock <= p.stockCritico order by p.stock asc")
    List<Producto> buscarCriticos();

    /**
     * Bloquea las filas de los productos durante la compra (SELECT ... FOR UPDATE)
     * para que dos compras simultáneas no vendan el mismo stock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.id in :ids")
    List<Producto> bloquearParaCompra(@Param("ids") Collection<Long> ids);

    long countByCategoriaId(Long categoriaId);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
