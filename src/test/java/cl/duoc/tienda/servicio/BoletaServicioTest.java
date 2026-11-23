package cl.duoc.tienda.servicio;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tienda.dto.BoletaDto;
import cl.duoc.tienda.dto.CompraRequest;
import cl.duoc.tienda.excepcion.DatosInvalidosException;
import cl.duoc.tienda.excepcion.ReglaNegocioException;
import cl.duoc.tienda.modelo.EstadoBoleta;
import cl.duoc.tienda.modelo.MetodoPago;
import cl.duoc.tienda.modelo.TipoEntrega;
import cl.duoc.tienda.repositorio.ProductoRepositorio;

/** Prueba la lógica de compra con la base H2 y los datos iniciales. Cada prueba se revierte al terminar. */
@SpringBootTest
@Transactional
class BoletaServicioTest {

    @Autowired
    private BoletaServicio servicio;

    @Autowired
    private ProductoRepositorio productos;

    private static final CompraRequest.Cliente CLIENTE = new CompraRequest.Cliente("Ana", "Rojas", "ana@gmail.com");
    private static final CompraRequest.Direccion DIRECCION = new CompraRequest.Direccion("Calle 1", "", "Metropolitana", "Santiago", "");

    private static CompraRequest compra(TipoEntrega entrega, boolean rechazo, CompraRequest.Item... items) {
        return new CompraRequest(CLIENTE, DIRECCION, entrega, MetodoPago.DEBITO, List.of(items), rechazo);
    }

    private static CompraRequest.Item item(long productoId, int cantidad) {
        return new CompraRequest.Item(productoId, cantidad);
    }

    private int stock(long id) {
        return productos.findById(id).orElseThrow().getStock();
    }

    @Test
    void compraPagadaDescuentaStockYUsaElPrecioDeOferta() {
        // Producto 4: Leche, precio 1050, oferta 890, stock 40
        BoletaDto boleta = servicio.procesarCompra(compra(TipoEntrega.DOMICILIO, false, item(4, 3)), null);

        assertThat(boleta.estado()).isEqualTo(EstadoBoleta.PAGADA);
        assertThat(boleta.codigo()).isEqualTo(BoletaServicio.generarCodigo(boleta.id()));
        assertThat(boleta.subtotal()).isEqualTo(890 * 3);
        assertThat(boleta.envio()).isEqualTo(BoletaServicio.COSTO_ENVIO);
        assertThat(boleta.total()).isEqualTo(890 * 3 + 2000);
        assertThat(stock(4)).isEqualTo(37);
    }

    @Test
    void rechazaPorStockInsuficienteSinTocarElInventario() {
        // Producto 7: Pack 6 Cervezas, stock 3
        BoletaDto boleta = servicio.procesarCompra(compra(TipoEntrega.RETIRO, false, item(7, 10)), null);

        assertThat(boleta.estado()).isEqualTo(EstadoBoleta.RECHAZADA);
        assertThat(boleta.motivoRechazo()).contains("Pack 6 Cervezas");
        assertThat(stock(7)).isEqualTo(3);
    }

    @Test
    void rechazaCuandoSeSimulaUnPagoRechazado() {
        BoletaDto boleta = servicio.procesarCompra(compra(TipoEntrega.RETIRO, true, item(1, 1)), null);

        assertThat(boleta.estado()).isEqualTo(EstadoBoleta.RECHAZADA);
        assertThat(boleta.motivoRechazo()).contains("rechazado");
        assertThat(stock(1)).isEqualTo(50);
    }

    @Test
    void sumaLasCantidadesDeUnMismoProducto() {
        BoletaDto boleta = servicio.procesarCompra(compra(TipoEntrega.RETIRO, false, item(1, 2), item(1, 1)), null);

        assertThat(boleta.items()).hasSize(1);
        assertThat(boleta.items().get(0).cantidad()).isEqualTo(3);
        assertThat(stock(1)).isEqualTo(47);
    }

    @Test
    void elEnvioEsGratisDesdeVeinteMilYEnRetiroEnTienda() {
        assertThat(BoletaServicio.calcularEnvio(19_999, TipoEntrega.DOMICILIO)).isEqualTo(2000);
        assertThat(BoletaServicio.calcularEnvio(20_000, TipoEntrega.DOMICILIO)).isZero();
        assertThat(BoletaServicio.calcularEnvio(5_000, TipoEntrega.RETIRO)).isZero();
    }

    @Test
    void elDespachoADomicilioExigeLaCalle() {
        CompraRequest sinCalle = new CompraRequest(CLIENTE, new CompraRequest.Direccion("", null, "Metropolitana", "Santiago", null),
                TipoEntrega.DOMICILIO, MetodoPago.DEBITO, List.of(item(1, 1)), false);

        assertThatThrownBy(() -> servicio.procesarCompra(sinCalle, null))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("calle");
    }

    @Test
    void fallaSiUnProductoNoExiste() {
        assertThatThrownBy(() -> servicio.procesarCompra(compra(TipoEntrega.RETIRO, false, item(999, 1)), null))
                .isInstanceOf(ReglaNegocioException.class);
    }

    @Test
    void unaBoletaRechazadaNoPuedeCambiarDeEstado() {
        BoletaDto rechazada = servicio.procesarCompra(compra(TipoEntrega.RETIRO, true, item(1, 1)), null);

        assertThatThrownBy(() -> servicio.cambiarEstado(rechazada.id(), EstadoBoleta.ENVIADA))
                .isInstanceOf(ReglaNegocioException.class);
    }
}
