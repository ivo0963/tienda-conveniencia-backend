package cl.duoc.tienda.servicio;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.duoc.tienda.dto.BoletaDto;
import cl.duoc.tienda.dto.CompraRequest;
import cl.duoc.tienda.excepcion.DatosInvalidosException;
import cl.duoc.tienda.excepcion.RecursoNoEncontradoException;
import cl.duoc.tienda.excepcion.ReglaNegocioException;
import cl.duoc.tienda.modelo.Boleta;
import cl.duoc.tienda.modelo.DetalleBoleta;
import cl.duoc.tienda.modelo.EstadoBoleta;
import cl.duoc.tienda.modelo.Producto;
import cl.duoc.tienda.modelo.Rol;
import cl.duoc.tienda.modelo.TipoEntrega;
import cl.duoc.tienda.repositorio.BoletaRepositorio;
import cl.duoc.tienda.repositorio.ProductoRepositorio;
import cl.duoc.tienda.repositorio.UsuarioRepositorio;

@Service
public class BoletaServicio {

    public static final int COSTO_ENVIO = 2000;
    public static final int ENVIO_GRATIS_DESDE = 20000;

    private final BoletaRepositorio boletas;
    private final ProductoRepositorio productos;
    private final UsuarioRepositorio usuarios;

    public BoletaServicio(BoletaRepositorio boletas, ProductoRepositorio productos, UsuarioRepositorio usuarios) {
        this.boletas = boletas;
        this.productos = productos;
        this.usuarios = usuarios;
    }

    /** Despacho: $2.000, gratis desde $20.000. El retiro en tienda no tiene costo. */
    public static int calcularEnvio(int subtotal, TipoEntrega entrega) {
        if (entrega == TipoEntrega.RETIRO || subtotal == 0 || subtotal >= ENVIO_GRATIS_DESDE) {
            return 0;
        }
        return COSTO_ENVIO;
    }

    /**
     * Registra una compra. Los precios se toman de la base de datos (nunca del cliente).
     * Si falta stock o el pago es rechazado, la boleta queda RECHAZADA y el stock no cambia.
     *
     * @param usuarioId usuario con sesión, o null si compra como invitado
     */
    @Transactional
    public BoletaDto procesarCompra(CompraRequest datos, Long usuarioId) {
        validarDireccion(datos);

        // Suma las cantidades si el mismo producto viene en dos líneas.
        Map<Long, Integer> cantidades = datos.items().stream()
                .collect(Collectors.toMap(CompraRequest.Item::productoId, CompraRequest.Item::cantidad, Integer::sum, LinkedHashMap::new));

        Map<Long, Producto> encontrados = productos.bloquearParaCompra(cantidades.keySet()).stream()
                .collect(Collectors.toMap(Producto::getId, Function.identity()));
        cantidades.keySet().stream()
                .filter(id -> !encontrados.containsKey(id))
                .findFirst()
                .ifPresent(id -> {
                    throw new ReglaNegocioException("El producto con id " + id + " ya no está disponible.");
                });

        Boleta boleta = new Boleta();
        boleta.setFecha(LocalDateTime.now());
        if (usuarioId != null) {
            boleta.setUsuario(usuarios.getReferenceById(usuarioId));
        }
        boleta.setClienteNombre(datos.cliente().nombre().trim());
        boleta.setClienteApellidos(datos.cliente().apellidos().trim());
        boleta.setClienteCorreo(datos.cliente().correo().trim());
        boleta.setEntrega(datos.entrega());
        boleta.setMetodoPago(datos.metodoPago());
        if (datos.entrega() == TipoEntrega.DOMICILIO) {
            CompraRequest.Direccion d = datos.direccion();
            boleta.setCalle(d.calle().trim());
            boleta.setDepartamento(d.departamento());
            boleta.setRegion(d.region());
            boleta.setComuna(d.comuna());
            boleta.setIndicaciones(d.indicaciones());
        }

        cantidades.forEach((id, cantidad) -> boleta.agregarDetalle(new DetalleBoleta(encontrados.get(id), cantidad)));
        int subtotal = boleta.getDetalles().stream().mapToInt(DetalleBoleta::getSubtotal).sum();
        int envio = calcularEnvio(subtotal, datos.entrega());
        boleta.setSubtotal(subtotal);
        boleta.setEnvio(envio);
        boleta.setTotal(subtotal + envio);

        List<String> sinStock = cantidades.entrySet().stream()
                .filter(e -> encontrados.get(e.getKey()).getStock() < e.getValue())
                .map(e -> encontrados.get(e.getKey()).getNombre())
                .toList();

        String motivoRechazo = null;
        if (!sinStock.isEmpty()) {
            motivoRechazo = "Stock insuficiente para: " + String.join(", ", sinStock) + ".";
        } else if (datos.debeSimularRechazo()) {
            motivoRechazo = "El medio de pago fue rechazado por el banco emisor.";
        }

        if (motivoRechazo == null) {
            boleta.setEstado(EstadoBoleta.PAGADA);
            cantidades.forEach((id, cantidad) -> {
                Producto p = encontrados.get(id);
                p.setStock(p.getStock() - cantidad);
            });
        } else {
            boleta.setEstado(EstadoBoleta.RECHAZADA);
            boleta.setMotivoRechazo(motivoRechazo);
        }

        Boleta guardada = boletas.save(boleta);
        guardada.setCodigo(generarCodigo(guardada.getId()));
        return BoletaDto.desde(guardada);
    }

    @Transactional(readOnly = true)
    public List<BoletaDto> listar() {
        return boletas.findAllByOrderByFechaDesc().stream().map(BoletaDto::desde).toList();
    }

    /** Administradores y vendedores ven cualquier boleta; un cliente solo las suyas. */
    @Transactional(readOnly = true)
    public BoletaDto obtener(Long id, Long usuarioId, Rol rol) {
        Boleta b = buscar(id);
        boolean esPersonal = rol == Rol.ADMIN || rol == Rol.VENDEDOR;
        boolean esDueno = b.getUsuario() != null && b.getUsuario().getId().equals(usuarioId);
        if (!esPersonal && !esDueno) {
            throw new AccessDeniedException("No tienes permiso para ver esta boleta.");
        }
        return BoletaDto.desde(b);
    }

    @Transactional(readOnly = true)
    public List<BoletaDto> listarDeUsuario(Long usuarioId) {
        String correo = usuarios.findById(usuarioId).map(u -> u.getCorreo()).orElse("");
        return boletas.buscarHistorial(usuarioId, correo).stream().map(BoletaDto::desde).toList();
    }

    @Transactional
    public BoletaDto cambiarEstado(Long id, EstadoBoleta nuevo) {
        Boleta b = buscar(id);
        if (b.getEstado() == EstadoBoleta.RECHAZADA) {
            throw new ReglaNegocioException("Una boleta con pago rechazado no puede cambiar de estado.");
        }
        if (nuevo == EstadoBoleta.RECHAZADA) {
            throw new ReglaNegocioException("No se puede marcar como rechazada una boleta ya pagada.");
        }
        b.setEstado(nuevo);
        return BoletaDto.desde(b);
    }

    public static String generarCodigo(Long id) {
        return String.format("ORD%05d", id);
    }

    private Boleta buscar(Long id) {
        return boletas.findWithDetallesById(id).orElseThrow(() -> new RecursoNoEncontradoException("Boleta", id));
    }

    private static void validarDireccion(CompraRequest datos) {
        if (datos.entrega() != TipoEntrega.DOMICILIO) {
            return;
        }
        CompraRequest.Direccion d = datos.direccion();
        if (d == null || d.calle() == null || d.calle().isBlank()) {
            throw new DatosInvalidosException("calle", "La calle es obligatoria.");
        }
        if (d.region() == null || d.region().isBlank()) {
            throw new DatosInvalidosException("region", "Debe seleccionar una región.");
        }
        if (d.comuna() == null || d.comuna().isBlank()) {
            throw new DatosInvalidosException("comuna", "Debe seleccionar una comuna.");
        }
    }
}
