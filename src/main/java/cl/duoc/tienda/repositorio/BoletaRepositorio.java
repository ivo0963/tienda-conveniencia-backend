package cl.duoc.tienda.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.duoc.tienda.modelo.Boleta;
import cl.duoc.tienda.modelo.EstadoBoleta;

public interface BoletaRepositorio extends JpaRepository<Boleta, Long> {

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    List<Boleta> findAllByOrderByFechaDesc();

    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    Optional<Boleta> findWithDetallesById(Long id);

    /** Historial de un usuario: sus compras con sesión y las hechas como invitado con su correo. */
    @EntityGraph(attributePaths = {"detalles", "detalles.producto", "usuario"})
    @Query("select b from Boleta b where b.usuario.id = :usuarioId or lower(b.clienteCorreo) = lower(:correo) order by b.fecha desc")
    List<Boleta> buscarHistorial(@Param("usuarioId") Long usuarioId, @Param("correo") String correo);

    long countByEstadoNot(EstadoBoleta estado);
}
